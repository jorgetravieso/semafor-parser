package parser;


import org.junit.Assert;
import org.junit.Test;
import stanfordparser.StanfordParser;

/**
 * @author jtravieso
 * @since 1/18/16.
 */

public class TestStanfordParse {


    StanfordParser stanfordParser = StanfordParser.getInstance();

    @Test
    public void testParses1() {
        String sentence = "I need help with my invoice.";
        String expectedOut = "7\tI\tneed\thelp\twith\tmy\tinvoice\t.\tPRP\tMD\tVB\tIN\tPRP$\tNN\t.\tSUB\tROOT\tVC\tVMOD\tNMOD\tPMOD\tP\t2\t0\t2\t3\t6\t4\t2\tO\tO\tO\tO\tO\tO\tO\ti\tneed\thelp\twith\tmy\tinvoice\t.";
        String stanfordOut = stanfordParser.parse(sentence).stream().findFirst().get().toString();
        Assert.assertEquals("should be the same parse", expectedOut, stanfordOut);
    }

    @Test
    public void testParses2() {
        String sentence = "The dog is very big and is scary.";
        String expectedOut = "9\tThe\tdog\tis\tvery\tbig\tand\tis\tscary\t.\tDT\tNN\tVBZ\tRB\tJJ\tCC\tVBZ\tJJ\t.\tNMOD\tSUB\tROOT\tAMOD\tPRD\tVMOD\tVMOD\tPRD\tP\t2\t3\t0\t5\t3\t3\t3\t7\t3\tO\tO\tO\tO\tO\tO\tO\tO\tO\tthe\tdog\tbe\tvery\tbig\tand\tbe\tscary\t.";
        String stanfordOut = stanfordParser.parse(sentence).stream().findFirst().get().toString();
        Assert.assertEquals("should be the same parse", expectedOut, stanfordOut);
    }


    @Test
    public void testParsesNE() {
        String sentence = "John is scared of dogs.";
        String expectedOut = "6\tJohn\tis\tscared\tof\tdogs\t.\tNNP\tVBZ\tVBN\tIN\tNNS\t.\tSUB\tROOT\tVC\tVMOD\tPMOD\tP\t2\t0\t2\t3\t4\t2\tO\tO\tO\tO\tO\tO\tjohn\tbe\tscare\tof\tdog\t.";
        String stanfordOut = stanfordParser.parse(sentence).stream().findFirst().get().toString();
        Assert.assertEquals("should be the same parse", expectedOut, stanfordOut);
    }


    @Test
    public void testParses() {
        String sentence = "John Smith likes to play soccer during winter.";
        String expectedOut = "9\tJohn\tSmith\tlikes\tto\tplay\tsoccer\tduring\twinter\t.\tNNP\tNNP\tVBZ\tTO\tVB\tNN\tIN\tNN\t.\tNMOD\tSUB\tROOT\tVMOD\tVMOD\tOBJ\tVMOD\tPMOD\tP\t2\t3\t0\t5\t3\t5\t5\t7\t3\tO\tO\tO\tO\tO\tO\tO\tO\tO\tjohn\tsmith\tlike\tto\tplay\tsoccer\tduring\twinter\t.";
        String stanfordOut = stanfordParser.parse(sentence).stream().findFirst().get().toString();
        Assert.assertEquals("should be the same parse", expectedOut, stanfordOut);
    }


}
