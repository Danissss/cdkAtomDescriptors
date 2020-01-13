package Xuan.cdkAtomDescriptors;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.simple.JSONObject;


public class GetDecriptors {
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args){

        Options options = new Options();
        
        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);
        
        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        options.addOption(output);
        
        Option atomtype = new Option("a", "atomtype", true, "atom type");
        atomtype.setRequired(true);
        options.addOption(atomtype);
        
        Option nearest = new Option("n", "nearest", true, "nearest atom");
        nearest.setRequired(true);
        options.addOption(nearest);
        
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        JSONObject JsonObj = new JSONObject(); // write error msg 

        try {
        		CommandLine cmd = parser.parse(options, args);
        		GetAtomDescriptors gadescriptor = new GetAtomDescriptors();
        		gadescriptor.execute(cmd);
        		
        } catch (ParseException e) {
            formatter.printHelp("utility-name", options);
            JsonObj.put("Error", e.getMessage());
            System.out.println(JsonObj.toJSONString());
        } catch (IOException e) {
        		JsonObj.put("Error", e.getMessage());
        		System.out.println(JsonObj.toJSONString());
		}
        
        
        
        
//        String inputFilePath = cmd.getOptionValue("input");
//        String outputFilePath = cmd.getOptionValue("output");
//
//        System.out.println(inputFilePath);
//        System.out.println(outputFilePath);

    }
	
	
}
