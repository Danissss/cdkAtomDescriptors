package cdkAtomDescriptors;


// What is import static? Useful for assert module.
// https://stackoverflow.com/questions/162187/what-does-the-static-modifier-after-import-mean
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Xuan.cdkAtomDescriptors.GetAtomDescriptors;

/**
 * Unit test for simple App.
 * run all test: $ mvn test
 */
public class BehaviourTest {
	private static String current_dir = System.getProperty("user.dir");
	
	@Test
    public void testMainFunction() throws IOException {
		
		String mol = "2244\n" + 
				"  -OEChem-01102018403D\n" + 
				"\n" + 
				" 21 21  0     0  0  0  0  0  0999 V2000\n" + 
				"    1.2333    0.5540    0.7792 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -0.6952   -2.7148   -0.7502 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"    0.7958   -2.1843    0.8685 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"    1.7813    0.8105   -1.4821 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -0.0857    0.6088    0.4403 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -0.7927   -0.5515    0.1244 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -0.7288    1.8464    0.4133 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -2.1426   -0.4741   -0.2184 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -2.0787    1.9238    0.0706 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -2.7855    0.7636   -0.2453 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -0.1409   -1.8536    0.1477 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"    2.1094    0.6715   -0.3113 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"    3.5305    0.5996    0.1635 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -0.1851    2.7545    0.6593 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -2.7247   -1.3605   -0.4564 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -2.5797    2.8872    0.0506 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -3.8374    0.8238   -0.5090 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"    3.7290    1.4184    0.8593 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"    4.2045    0.6969   -0.6924 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"    3.7105   -0.3659    0.6426 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"   -0.2555   -3.5916   -0.7337 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
				"  1  5  1  0  0  0  0\n" + 
				"  1 12  1  0  0  0  0\n" + 
				"  2 11  1  0  0  0  0\n" + 
				"  2 21  1  0  0  0  0\n" + 
				"  3 11  2  0  0  0  0\n" + 
				"  4 12  2  0  0  0  0\n" + 
				"  5  6  1  0  0  0  0\n" + 
				"  5  7  2  0  0  0  0\n" + 
				"  6  8  2  0  0  0  0\n" + 
				"  6 11  1  0  0  0  0\n" + 
				"  7  9  1  0  0  0  0\n" + 
				"  7 14  1  0  0  0  0\n" + 
				"  8 10  1  0  0  0  0\n" + 
				"  8 15  1  0  0  0  0\n" + 
				"  9 10  2  0  0  0  0\n" + 
				"  9 16  1  0  0  0  0\n" + 
				" 10 17  1  0  0  0  0\n" + 
				" 12 13  1  0  0  0  0\n" + 
				" 13 18  1  0  0  0  0\n" + 
				" 13 19  1  0  0  0  0\n" + 
				" 13 20  1  0  0  0  0\n" + 
				"M  END";
		
		String inputFile = String.format("%s/%s", current_dir,"tmpMol.sdf");
		String outputFile = String.format("%s/%s", current_dir,"tmpMol.json");
		PrintWriter writer = new PrintWriter(inputFile, "UTF-8");
		writer.write(mol);
		writer.close();
		
		GetAtomDescriptors gtd = new GetAtomDescriptors();
		gtd.runCDKAtomDescriptors(inputFile, outputFile, "H", "3");
		
		File file = new File(inputFile);
		if(file.exists()) { file.delete(); }
		
		File outFile = new File(outputFile);
		FileInputStream fis = new FileInputStream(outFile);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
	
		String JsonContent = new String(data, "UTF-8");
	
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		JsonNode rootNode = mapper.readTree(JsonContent);  

		Iterator<Map.Entry<String,JsonNode>> fieldsIterator = rootNode.fields();
		HashMap<String,Object> valueHolder = new HashMap<String,Object>();
		while (fieldsIterator.hasNext()) {
			Map.Entry<String,JsonNode> field = fieldsIterator.next();
			valueHolder.put(field.getKey(),field.getValue());
		}
		
		assertEquals(valueHolder.get("atomtype"),"H");
		
	
		File jsonFile = new File(outputFile);
		if(jsonFile.exists()) { file.delete(); }
		
		
	}
}















































