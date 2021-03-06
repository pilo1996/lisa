package it.unive.lisa.program.cfg.statement;

import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.caches.Caches;
import it.unive.lisa.callgraph.CallGraph;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.Untyped;
import it.unive.lisa.util.collections.externalSet.ExternalSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * An expression that is part of a statement of the program.
 * 
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public abstract class Expression extends Statement {

	/**
	 * The static type of this expression.
	 */
	private final Type staticType;

	/**
	 * The set of runtime types of this expression, only computed if type
	 * inference is executed.
	 */
	private ExternalSet<Type> runtimeTypes;

	/**
	 * The statement (or expression) that contains this expression.
	 */
	private Statement parent;

	/**
	 * The collection of meta variables that are generated by the evaluation of
	 * this expression. These should be removed as soon as the values computed
	 * by those gets out of scope (e.g., popped from the stack).
	 */
	private final Collection<Identifier> metaVariables;

	/**
	 * Builds an untyped expression happening at the given source location, that
	 * is its type is {@link Untyped#INSTANCE}.
	 * 
	 * @param cfg      the cfg that this expression belongs to
	 * @param location the location where the expression is defined within the
	 *                     source file. If unknown, use {@code null}
	 */
	protected Expression(CFG cfg, CodeLocation location) {
		this(cfg, location, Untyped.INSTANCE);
	}

	/**
	 * Builds a typed expression happening at the given source location.
	 * 
	 * @param cfg        the cfg that this expression belongs to
	 * @param location   the location where this expression is defined within
	 *                       the source file. If unknown, use {@code null}
	 * @param staticType the static type of this expression
	 */
	protected Expression(CFG cfg, CodeLocation location, Type staticType) {
		super(cfg, location);
		Objects.requireNonNull(staticType, "The expression type of a CFG cannot be null");
		this.staticType = staticType;
		this.metaVariables = new HashSet<>();
	}

	/**
	 * Yields the static type of this expression.
	 * 
	 * @return the static type of this expression
	 */
	public final Type getStaticType() {
		return staticType;
	}

	/**
	 * Sets the runtime types of this expression.
	 * 
	 * @param runtimeTypes the set of concrete types that this expression can
	 *                         have at runtime
	 */
	public final void setRuntimeTypes(ExternalSet<Type> runtimeTypes) {
		if (runtimeTypes == null)
			return;

		if (this.runtimeTypes != null && (this.runtimeTypes == runtimeTypes || this.runtimeTypes.equals(runtimeTypes)))
			return;

		if (this.runtimeTypes != null && runtimeTypes.isEmpty())
			this.runtimeTypes.clear();
		else
			this.runtimeTypes = runtimeTypes.copy();
	}

	/**
	 * The concrete types that this expression can have at runtime. If type
	 * inference has not been executed, this method returns a singleton set
	 * containing the static type of this expression.
	 * 
	 * @return the set of runtime types
	 */
	public final ExternalSet<Type> getRuntimeTypes() {
		if (runtimeTypes == null)
			return Caches.types().mkSet(staticType.allInstances());
		return runtimeTypes;
	}

	/**
	 * Yields the dynamic type of this expression, that is, the most specific
	 * common supertype of all its runtime types (available through
	 * {@link #getRuntimeTypes()}.
	 * 
	 * @return the dynamic type of this expression
	 */
	public final Type getDynamicType() {
		ExternalSet<Type> runtimes = getRuntimeTypes();
		return getRuntimeTypes().reduce(runtimes.first(), (result, t) -> {
			if (result.canBeAssignedTo(t))
				return t;
			if (t.canBeAssignedTo(result))
				return result;
			return t.commonSupertype(result);
		});
	}

	/**
	 * Yields the meta variables that are generated by the evaluation of this
	 * expression. These should be removed as soon as the values computed by
	 * those gets out of scope (e.g., popped from the stack). The returned
	 * collection will be filled while evaluating this expression
	 * {@link #semantics(AnalysisState, CallGraph, StatementStore)}, thus
	 * invoking this method before computing the semantics will yield an empty
	 * collection.
	 * 
	 * @return the meta variables
	 */
	public Collection<Identifier> getMetaVariables() {
		return metaVariables;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((staticType == null) ? 0 : staticType.hashCode());
		// we ignore the meta variables on purpose
		return result;
	}

	@Override
	public boolean isEqualTo(Statement st) {
		if (this == st)
			return true;
		if (!super.isEqualTo(st))
			return false;
		if (getClass() != st.getClass())
			return false;
		Expression other = (Expression) st;
		if (staticType == null) {
			if (other.staticType != null)
				return false;
		} else if (!staticType.equals(other.staticType))
			return false;
		// we ignore the meta variables on purpose
		return true;
	}

	/**
	 * Sets the {@link Statement} that contains this expression.
	 * 
	 * @param st the containing statement
	 */
	public final void setParentStatement(Statement st) {
		this.parent = st;
	}

	/**
	 * Yields the {@link Statement} that contains this expression, if any. If
	 * this method returns {@code null}, than this expression is used as a
	 * command: it is the root statement of a node in the cfg, and its returned
	 * value is discarded.
	 * 
	 * @return the statement that contains this expression, if any
	 */
	public final Statement getParentStatement() {
		return parent;
	}

	/**
	 * Yields the outer-most {@link Statement} containing this expression, that
	 * is used as a node in the cfg. If this expression is used a command, then
	 * this method return {@code this}.
	 * 
	 * @return the outer-most statement containing this expression, or
	 *             {@code this}
	 */
	public final Statement getRootStatement() {
		if (parent == null)
			return this;

		if (!(parent instanceof Expression))
			return parent;

		return ((Expression) parent).getRootStatement();
	}
}
