/**
 * Notice:
 * 1. For those descriptor that return more than one value (in an array), we only extract first value
 * 2. NaN is preserved so the user can decide how to deal with missing value based on their need
 */
package Xuan.cdkAtomDescriptors;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.graph.invariant.MorganNumbersTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.IDescriptor;
import org.openscience.cdk.qsar.IAtomicDescriptor;
import org.openscience.cdk.qsar.result.BooleanResult;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.qsar.result.IntegerArrayResult;
import org.openscience.cdk.qsar.result.IntegerResult;
import org.openscience.cdk.tools.HOSECodeGenerator;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.openscience.cdk.exception.CDKException;

import javax.vecmath.Point3d;


public class GetAtomDescriptors {
	 private static List<String> classNames = DescriptorEngine.getDescriptorClassNameByPackage("org.openscience.cdk.qsar.descriptors.atomic",null);
	 private static DescriptorEngine engine = new DescriptorEngine(classNames, null);


	/**
	 * Example main
	 * input: molecule file (has be 3D), output file, atom type, nearest atoms
	 * return: json output or json file 
	 * @throws IOException 
	 */
	public void execute(CommandLine cmd) throws IOException {
		
		String inputFilePath = cmd.getOptionValue("input");
		String outputFilePath = cmd.getOptionValue("output");
		String atom_type = cmd.getOptionValue("atomtype");
		String nearest = cmd.getOptionValue("nearest");
		
		JSONObject JsonObj = runCDKAtomDescriptors(inputFilePath, outputFilePath,atom_type,nearest);
		
		writeJsonFile(outputFilePath, JsonObj); 
	}
	
	/**
	 * Example main
	 * input: molecule file (has be 3D), output file, atom type, nearest atoms
	 * return: json output or json file 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public JSONObject runCDKAtomDescriptors(String inputFilePath, String outputFilePath, String atom_type,
							String nearest) throws IOException {

		int nearest_atoms = Integer.valueOf(nearest);
		
		GetAtomDescriptors descriptorUtil = new GetAtomDescriptors();
		
		List<IDescriptor> descriptorList = engine.getDescriptorInstances();
		ArrayList<String> descriptorNames = descriptorUtil.getDescriptorNames(descriptorList);
//		System.out.println(descriptorNames.size());
		IAtomContainer molecule = descriptorUtil.convertSdfToIAtomContainer(inputFilePath);
		
		ArrayList<ArrayList<Double>> atomDescriptor = calculateAtomDescriptors( molecule, descriptorList); // each atom has at least 27 descriptor values
		ArrayList<ArrayList<Integer>> nearestAtoms =  getNearestAtoms(molecule);
		
		
		HashMap<Integer,ArrayList<Double>> descriptorValue = descriptorUtil.getAtomicDescriptor(molecule, 
																descriptorList, descriptorNames, atomDescriptor,
																nearestAtoms, atom_type, nearest_atoms);

		long[] morganNumber = descriptorUtil.getMorganNumber(molecule);

		JSONObject JsonObj = new JSONObject();
		JsonObj.put("AtomType", atom_type);
		JsonObj.put("NearestAtoms", nearest_atoms);

		JSONArray morganNumberJsonArray = new JSONArray();
		for(int i = 0; i < morganNumber.length; i++) {
			System.out.println(morganNumber[i]);
			morganNumberJsonArray.add(morganNumber[i]);
		}
		JsonObj.put("MorganNumber", morganNumberJsonArray);

		JSONObject DescriptorValue = new JSONObject();
		for(Integer atom_position : descriptorValue.keySet()) {
 			JSONArray JsonArray = new JSONArray();
			JsonArray.addAll(descriptorValue.get(atom_position));
			DescriptorValue.put(atom_position, JsonArray);
		}
		JsonObj.put("Descriptors", DescriptorValue);
		
		JSONObject atomDescriptorValue = new JSONObject();
		JSONObject atomNearestAtoms    = new JSONObject();
		JSONObject atomType 			   = new JSONObject();
		for(int atom = 0; atom < atomDescriptor.size(); atom++) {
			JSONArray JsonArray = new JSONArray();
			JSONArray JsonArrayNA = new JSONArray();
			JsonArray.addAll(atomDescriptor.get(atom));
			JsonArrayNA.addAll(nearestAtoms.get(atom));
			atomType.put(atom, molecule.getAtom(atom).getSymbol());
			atomDescriptorValue.put(atom, JsonArray);
			atomNearestAtoms.put(atom, JsonArrayNA);
		}
		JsonObj.put("AtomDescriptorValue", atomDescriptorValue);
		JsonObj.put("AtomNearestAtoms", atomNearestAtoms);
		JsonObj.put("AtomSymbol", atomType);
		
		

		JSONArray descriptorNamesInJason = new JSONArray();
		for(int i = 0; i < nearest_atoms; i++) {
			descriptorNamesInJason.addAll(descriptorNames);
		}
		JsonObj.put("DescriptorsName", descriptorNamesInJason);
		
		return JsonObj;
	}
	
	
	/**
	 * 
	 * @param outputFile
	 * @param JsonObj
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static void writeJsonFile(String outputFile, JSONObject JsonObj) 
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		writer.writeValue(new File(outputFile), JsonObj);
	}
	
	
	/**
	 * @param nearestAtoms
	 */
	public static void printNearestAtoms(ArrayList<ArrayList<String>> nearestAtoms) {
		for (int i = 0; i < nearestAtoms.size(); i++) {
			System.out.println(nearestAtoms.get(i).toString());
		}
	}
	
	/** 
	 * @param descriptorValue
	 */
	public static void printDescriptorValue(ArrayList<Double> descriptorValue) {
		System.out.println(descriptorValue.toString());
	}
	
	/**
	 * 
	 * @param descriptorList
	 * @return
	 */
	public ArrayList<String> getDescriptorNames(List<IDescriptor> descriptorList) {
		ArrayList<String> descriptorNames = new ArrayList<String>();
		for(int i = 0; i < descriptorList.size(); i++) {
			String[] tmp = descriptorList.get(i).getDescriptorNames();
			for(int d = 0; d < tmp.length; d++) {
				descriptorNames.add(tmp[d]);
			}
			
		}
		return descriptorNames;
	}
	
	/**
	 * 
	 * @param sdfPath
	 * @return
	 * @throws IOException
	 */
	public IAtomContainer convertSdfToIAtomContainer(String sdfPath) throws IOException {
		File sdfFile = new File(sdfPath);
		IteratingSDFReader reader = new IteratingSDFReader(
				new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());
		IAtomContainer molecule = reader.next();	
		reader.close();
		return molecule;
	}
	
	
	/**
	 *
	 *  
	 *  1. calculate the descriptors for each atoms and store in hashtable
	 *  2. calculate the nearest atom for each atoms and store in hashtable
	 *  3. put everything in json format
	 *  4. return atom_position: Double[] (descriptors)
	 */
	public HashMap<Integer,ArrayList<Double>> getAtomicDescriptor(IAtomContainer molecule, List<IDescriptor> descriptorList, 
			ArrayList<String> descriptorNames, ArrayList<ArrayList<Double>> atomDescriptor,
			ArrayList<ArrayList<Integer>> nearestAtoms,
			String atomType, int num_atoms) throws java.io.IOException {
		
		
		ArrayList<Integer> atoms_position = new ArrayList<Integer>();
		HashMap<Integer,ArrayList<Double>> result = new HashMap<Integer,ArrayList<Double>>();
		
		if (atomType.equals("H") || atomType.equals("h")) {
			atoms_position.addAll(getHydrogenAtoms(molecule));
		}else if (atomType.equals("C") ||  atomType.equals("c")) {
			atoms_position.addAll(getCarbonAtoms(molecule));
		}else {
			atoms_position.addAll(getAtoms(molecule,atomType.toUpperCase()));
		}
				
		for(int m = 0; m < atoms_position.size(); m++) {
			ArrayList<Double> temporaryDescriptorHolder = new ArrayList<Double>();
			temporaryDescriptorHolder.addAll(atomDescriptor.get(atoms_position.get(m)));
			for(int n = 0; n < num_atoms; n++) {
				temporaryDescriptorHolder.addAll(atomDescriptor.get(nearestAtoms.get(atoms_position.get(m)).get(n)));
			}
			result.put(atoms_position.get(m), temporaryDescriptorHolder);
		}
		
		return result;
	}

	
	/**
	 *  
	 * @param molecule
	 * @param descriptorList
	 * @return
	 */
	public ArrayList<ArrayList<Double>> calculateAtomDescriptors(IAtomContainer molecule, List<IDescriptor> descriptorList) {
		ArrayList<ArrayList<Double>> atomDescriptor = new ArrayList<ArrayList<Double>>();
		
		for(int i = 0; i < molecule.getAtomCount(); i++) {
//			System.out.println("calculating==============:=> "+molecule.getAtom(i).getSymbol());
			ArrayList<Double> tmp = computeDescriptorsAtomic(molecule,molecule.getAtom(i), descriptorList);
//			System.out.println(tmp.size());
//			System.out.println(tmp.toString());
			atomDescriptor.add(tmp);
		}
		
		return atomDescriptor;		
		
	}


	/**
	 * Get morgan number
	 * @param molecule
	 * @return long[]
	 */
	public long[] getMorganNumber(IAtomContainer molecule){
		MorganNumbersTools morganTools = new MorganNumbersTools();
		long[] morganNumber = morganTools.getMorganNumbers(molecule);
		return morganNumber;
	}

	/*
	 * 
	 *  works for getStructures from NmrExperiment
	 */
	public static ArrayList<Integer> getHydrogenAtoms(IAtomContainer mol) {
		ArrayList<Integer> hydrogen_positions = new ArrayList<Integer>();

		int atomCount = mol.getAtomCount();
		for (int i = 0; i < atomCount; i++) {
			IAtom atom = mol.getAtom(i);
			if (atom.getSymbol().equals("H")) {
				List<IAtom> connected_atoms = mol.getConnectedAtomsList(atom);
				if (!connected_atoms.get(0).getSymbol().equals("O") && !connected_atoms.get(0).getSymbol().equals("N")) {
					hydrogen_positions.add(i);
				}
			}
		}
		
		return hydrogen_positions;
	}
	
	/*
	 * 
	 *  works for getStructures from NmrExperiment
	 */
	public static ArrayList<Integer> getCarbonAtoms(IAtomContainer mol) {
		ArrayList<Integer> carbon_positions = new ArrayList<Integer>();

		int atomCount = mol.getAtomCount();
		for (int i = 0; i < atomCount; i++) {
			IAtom atom = mol.getAtom(i);
			if (atom.getSymbol().equals("O")) {
				carbon_positions.add(i);
			}
		}
		
		return carbon_positions;
	}
	
	/*
	 * 
	 *  works for getStructures from NmrExperiment
	 */
	public static ArrayList<Integer> getAtoms(IAtomContainer mol, String atomType) {
		ArrayList<Integer> atom_positions = new ArrayList<Integer>();
		int atomCount = mol.getAtomCount();
		for (int i = 0; i < atomCount; i++) {
			IAtom atom = mol.getAtom(i);
			if (atom.getSymbol().equals(atomType)) {
				atom_positions.add(i);
			}
		}
		
		return atom_positions;
	}
	
	
	// for NmrExperiment

	// Find nearest atom to all atoms in a molecule
	// 
	public static ArrayList<ArrayList<Integer>> getNearestAtoms(IAtomContainer mol) {
		ArrayList<ArrayList<Integer>> atom_distances = new ArrayList<ArrayList<Integer>>(); //2d array.
		
		List<IAtom> atoms = new ArrayList<IAtom>();
		for (int i = 0; i < mol.getAtomCount(); i++) {
			atoms.add(mol.getAtom(i)); // get each atoms of the given mol;
		}

		for (int i = 0; i < atoms.size(); i++) {
			Double[] distances = new Double[atoms.size()];
			
			for (int j = 0; j < atoms.size(); j++) {
				if (j == i) {
					distances[j] = 99999.12; // make the original atom at the end;
					continue;
				}
			
				Point3d firstPoint = atoms.get(i).getPoint3d();
				Point3d secondPoint = atoms.get(j).getPoint3d();
				Double distance = firstPoint.distance(secondPoint);
				distances[j] = distance;

			}

			ArrayList<String> indices = new ArrayList<String>();
			ArrayList<Integer> indices_int = new ArrayList<Integer>();
			Double[] d = distances.clone(); // clone the original list (unsorted)
			Arrays.sort(d); // sort;
			List<Double> d_list = Arrays.asList(distances); // put into

			for (int j = 0; j < distances.length; j++) {
				String index = String.valueOf(d_list.indexOf(d[j])); // get index of that changed atoms
				indices_int.add(d_list.indexOf(d[j]));
				indices.add(index);
			}

			atom_distances.add(indices_int);
			
		
		}
		
		return atom_distances;
	}
	
	
	
	
	
	/*
	 *  this function is for getAtomicDescriptor
	 */
	public static ArrayList<String> getHoseCodesForMolecule(IAtomContainer mol) {
		HOSECodeGenerator hoseG = new HOSECodeGenerator();
		ArrayList<String> hoseCodes = new ArrayList<String>();

		int atomCount = mol.getAtomCount();
		for (int i = 0; i < atomCount; i++) {
			try {
				String hose = hoseG.getHOSECode(mol, mol.getAtom(i), 0);
				hoseCodes.add(hose);
				
			} catch (CDKException e) {
				e.printStackTrace();
			}
		}
		return hoseCodes;
	}

	/**
	 * calculate all descriptor for one given atom
	 * @param mol
	 * @param atom
	 * @param descriptor
	 * @return
	 */
	public static ArrayList<Double> computeDescriptorsAtomic(IAtomContainer mol, IAtom atom, List<IDescriptor> descriptor) {
		ArrayList<Double> vv = new ArrayList<Double>();
		for(int i = 0; i < descriptor.size(); i++) {
			try {
				
				IDescriptorResult res = ((IAtomicDescriptor) descriptor.get(i)).calculate(atom, mol).getValue();
				
				if (res instanceof IntegerResult) {
					vv.add((double) ((IntegerResult) res).intValue());
					
				} else if (res instanceof DoubleResult) {
					vv.add(((DoubleResult) res).doubleValue());
				} else if (res instanceof DoubleArrayResult) {
//					for(int d = 0; d < ((DoubleArrayResult) res).length(); d++) {
//						vv.add(((DoubleArrayResult) res).get(d));
//					}
					
					// did this because of https://github.com/cdk/cdk/issues/620
					String[] descriptorName = ((IAtomicDescriptor) descriptor.get(i)).getDescriptorNames();
					for(int d = 0; d < descriptorName.length; d++) {
						vv.add(((DoubleArrayResult) res).get(d));
					}

					
				} else if (res instanceof IntegerArrayResult) {
					for(int d = 0; d < ((IntegerArrayResult) res).length(); d++) {
						vv.add(Double.valueOf(((IntegerArrayResult) res).get(d)));
					}
				} else if (res instanceof BooleanResult) {
					// true = 1.0; false = 0.0;
					if(((BooleanResult) res).booleanValue() == true) {
						vv.add(1.0);
					}else {
						vv.add(0.0);
					}
				}
				
				else {
					System.out.println("Error");
					throw new IllegalStateException(
							"Unknown idescriptor result value for '" + descriptor + "' : " + res.getClass());
				}
			} catch (Throwable e) {
				System.out.println("Could not compute cdk feature " + descriptor);
				System.err.println("Could not compute cdk feature " + descriptor);
				vv.add(0.0);
			}
		}
		
		
		
		return vv;
	}

}
