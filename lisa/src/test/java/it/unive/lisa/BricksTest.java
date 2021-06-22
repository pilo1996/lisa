package it.unive.lisa;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.impl.string.Bricks;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.Program;
import org.junit.Test;

import static it.unive.lisa.LiSAFactory.getDefaultFor;

public class BricksTest {

    @Test
    public void testBricks() throws AnalysisException, ParsingException {
        LiSAConfiguration conf = new LiSAConfiguration()
                .setDumpAnalysis(true)
                .setWorkdir("test-outputs/bricks")
                .setAbstractState(getDefaultFor(AbstractState.class, getDefaultFor(HeapDomain.class), new Bricks()));

        LiSA lisa = new LiSA(conf);

        Program program = IMPFrontend.processFile("imp-testcases/bricks/program.imp");

        lisa.run(program);
    }
}
