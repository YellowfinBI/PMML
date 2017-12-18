package pmmlAdv;

import com.google.common.collect.BiMap;
import com.hof.mi.interfaces.AnalyticalFunction;
import com.hof.mi.interfaces.UserInputParameters;
import com.hof.util.UtilString;
import com.hof.util.YFLogger;
import com.hof.util.compress.YuiCompressorErrorReporter;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.dmg.pmml.Entity;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.HasEntityId;
import org.jpmml.evaluator.HasEntityRegistry;
import org.jpmml.evaluator.HasProbability;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.InvalidResultException;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class pmmlyfNumeric
  extends AnalyticalFunction
{
  private Integer numfields = 0;
  private Object[] result = null;
  private boolean loaded = false;
  public Integer type = null;
  private String descText= getText("Select a PMML Model and click Load. This will return a numeric result.", "mi.text.advf.numeric.pmml.desc");
  private static final YFLogger log = YFLogger.getLogger(pmmlyfNumeric.class.getName());

  private UserInputParameters.Parameter loadswitch = new UserInputParameters.Parameter();
  private UserInputParameters.Parameter outputselect = new UserInputParameters.Parameter();
  private List<com.hof.mi.interfaces.UserInputParameters.Parameter> plist = new ArrayList<com.hof.mi.interfaces.UserInputParameters.Parameter>();
  
  private final static int MAX_ALLOWED_FIELDS_IN_MODEL = 25;
  
  @SuppressWarnings("unused")
  public boolean acceptsNativeType(int i) {
		return i==1;	
	}
  public Object applyAnalyticFunction(int j, Object val)
    throws Exception
  {
	if (result==null){
		return null;
	}	 
    return this.result[j];
   
  }
  public String getCategory()
  {
    return getText("Statistical", "mi.text.advf.numeric.pmml.category");
  }
  public String getColumnHeading(String arg0)
  {
    return getText("PMML Model", "mi.text.advf.numeric.pmml.columnheading");
  }
  public String getDescription()
  {
    return descText;
  }
  public String getName()
  {
    return getText("PMML Model (Numeric)", "mi.text.advf.numeric.pmml.name");
  }
  public int getReturnType()
  {
	//Currently have to pick one, and numeric seems to be the most generally appropriate 
    return 1;
  }
  protected void setupParameters()
  {

    com.hof.mi.interfaces.UserInputParameters.Parameter parameter = 
    new UserInputParameters.Parameter();
    parameter.setUniqueKey("filename");
    parameter.setDisplayName(getText("PMML File", "mi.text.advf.numeric.pmml.filename.display"));
    parameter.setDescription(getText("Complete path or URL for PMML file", "mi.text.advf.numeric.pmml.filename.description"));
	parameter.setDataType(UserInputParameters.TYPE_TEXT);
	parameter.setDisplayType(UserInputParameters.DISPLAY_TEXT_LONG);
	parameter.setDefaultValue(new String(""));
	addParameter(parameter);
    
	loadswitch.setUniqueKey("Load");
	loadswitch.setDisplayName(getText("Load PMML", "mi.text.advf.numeric.pmml.load.display"));
	loadswitch.setDescription("");
	loadswitch.setDataType(2);
	loadswitch.setDisplayType(7);
	loadswitch.addOption("No", getText("Reset", "mi.text.advf.numeric.pmml.load.options.reset"));
	loadswitch.addOption("Yes", getText("Load", "mi.text.advf.numeric.pmml.load.options.load"));
	loadswitch.setDefaultValue("No");
    addParameter(loadswitch);
    
    outputselect = new UserInputParameters.Parameter();
    outputselect.setUniqueKey("outputfield");
    outputselect.setDisplayName(getText("Model Output", "mi.text.advf.numeric.pmml.model.output.display"));
    outputselect.setDescription(getText("Select which output field you want to display", "mi.text.advf.numeric.pmml.model.output.description"));
    outputselect.setDataType(2);
    outputselect.setDisplayType(6);
    
	//Create 15 (all initially set to not required)
    addParameter(outputselect);
	for (int i=1;i<MAX_ALLOWED_FIELDS_IN_MODEL+1;i++){
		String ukey = "p"+i;
		String pkey = "Parameter "+i;
	    parameter = new UserInputParameters.Parameter();
	    parameter.setUniqueKey(ukey);
	    parameter.setDisplayName(pkey);
	    parameter.setDescription("");
	    parameter.setDataType(100);
	    parameter.setDisplayType(6);
	    parameter.setAcceptsFieldType(6, true);
	    parameter.setAcceptsFieldType(3, true);
	    parameter.setAcceptsFieldType(1, true);
	    parameter.setAcceptsFieldType(2, true);
	    parameter.setAcceptsFieldType(4, true);
	    parameter.setAcceptsFieldType(5, true);
	    addParameter(parameter);
	    plist.add(parameter);
	}

  }
  //Runs the evaluator (for pre-loading see isParameterRequired)
  public void preAnalyticFunction(Object[] paramArrayOfObject)
  {
		    PMML pmml = null;  
		    String returnfield = (String)getParameterValue("outputfield");
		    String filepath = (String)getParameterValue("filename");		    
		    File pmmlin;
		    InputStream is = null;
		    //load from file or URL
		    if (filepath.contains("http")){
				try {
					is = new URL(filepath).openStream();
					pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
				} catch (IOException|SAXException |JAXBException  e) {
					pmmlin=null;
			}}else{
				pmmlin = new File(filepath);
			    try{
			     is = new FileInputStream(pmmlin);
			   	 pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
			    } catch (IOException|SAXException |JAXBException e ) {
			}}
		    if (is!=null){ try {is.close();} catch (IOException e) {e.printStackTrace();}}

		    
			//Create Evaluator	
			ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
			Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
			evaluator.verify();
			
			
			List<InputField> inputFields = evaluator.getInputFields();
			
			ArrayList<FieldName> colnames= new ArrayList<FieldName>();
			List<Object []> data = new ArrayList<>(15);
			
			Integer ic =1;
			//Create table data matrix, and FieldName list
			for(InputField tinputField : inputFields){
				colnames.add(tinputField.getName());
		    	String keystr="p"+ic;
		    	Object [] temp = (Object[]) getParameterValue(keystr);
		    	data.add(temp);
		    	ic++;
			}
			
			//Create inputrecords list for evaluator
		    List<Number> resultz= new ArrayList<>(data.get(0).length);
			List<Map<FieldName, String>> inputRecords = new ArrayList<>(1);
			for (int i=0;i<data.get(0).length; i++){
				Map<FieldName, String> temp = new LinkedHashMap<>();
				for (int k=0;k<colnames.size(); k++){
					if (data.get(k)[i]!=null) {
						temp.put(colnames.get(k),data.get(k)[i].toString());
					}
					//the evaluator naturally accepts a string field then does the conversion in "prepare"
				}
			inputRecords.add(temp);
			}
			
			Number res1=null;
			
			Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
			for(Map<FieldName, ?> inputRecord : inputRecords){
				arguments.clear();
				
				for(InputField inputField : inputFields){
					FieldName name = inputField.getName();
					if (inputRecord.get(name)!=null&&!inputRecord.get(name).equals("")) {

					//Prepare...converts from string, to the expected data type
					try{
						arguments.put(name, inputField.prepare(inputRecord.get(name)));
					}
					catch (InvalidResultException e){
						log.warn("Invalid Input to field:"+ inputField.getName().toString(),e);
						return;
					}}
				}
				try{
					Map<FieldName, ?> results = evaluator.evaluate(arguments);
					
					//Get the "Target Field" result
					List<TargetField> targetFields = evaluator.getTargetFields();
					for(TargetField targetField : targetFields){
						FieldName targetFieldName = targetField.getName();
					    Object targetFieldValue = results.get(targetFieldName);
					    if(targetFieldValue instanceof Computable){
					    	Computable computable = (Computable)targetFieldValue;
					    	Object unboxedTargetFieldValue = computable.getResult();
						    if (unboxedTargetFieldValue instanceof Number){
						    	res1=(Number) unboxedTargetFieldValue;
						    }else{
						    	res1=Float.valueOf((String) unboxedTargetFieldValue);
						    }
						    String outputname = "Default Target";
						    if (targetFieldName!=null){
						    	outputname = targetFieldName.toString();
						    }
						    
						    // If this is the field selected
					    	if (returnfield.equals(outputname)){
					    		resultz.add(res1);
					    	}
					    }
					    //Some functions such as logit return the probability of the the winning classification
					    if(targetFieldValue instanceof HasEntityId){
					    	HasEntityId hasEntityId = (HasEntityId)targetFieldValue;
					    	HasEntityRegistry<?> hasEntityRegistry = (HasEntityRegistry<?>)evaluator;
					    	BiMap<String, ? extends Entity> entities = hasEntityRegistry.getEntityRegistry();
					    	Entity winner = entities.get(hasEntityId.getEntityId());
					    	if(targetFieldValue instanceof HasProbability){
					    		HasProbability hasProbability = (HasProbability)targetFieldValue;
					    		Double winnerProbability = hasProbability.getProbability(winner.getId());
					    	}
					    }
					}
					// Get the output fields' results
					List<OutputField> outputFields = evaluator.getOutputFields();
					for(OutputField outputField : outputFields){
						FieldName outputFieldName = outputField.getName();
					    Object outputFieldValue = results.get(outputFieldName);
					    if (outputFieldValue instanceof Number){
					    	res1=(Number) outputFieldValue;
					    }else{
					    	res1=Float.valueOf((String) outputFieldValue);
					    }
				    	if (returnfield.equals(outputFieldName.toString())){
				    		resultz.add(res1);
				    	}
					}
				}catch (Exception e){
					log.error(e.getMessage(),e);
					resultz.add(null);
				}


			}

		    this.result = resultz.toArray();
  }
  public boolean hasDependentParameters(String paramString)
  {
    if ("Load".equals(paramString)) {
      return true;
    }
    return false;
  }
  //Generates the user prompt options when the "Load" button is clicked
  public boolean isParameterRequired(String paramString){
	  //Try to load the specified PMML file, and generate additional user prompt labels
	  if (((String) getParameterValue("Load")).length() > 2 && this.loaded == false){
	  PMML pmml = null;
	    String filepath = (String)getParameterValue("filename");
	    InputStream is = null;
	    File pmmlin;
	    if (filepath.contains("http")){
	    	writeLoadError(getText("PMML: Loading from URL",  "mi.text.advf.numeric.pmml.loading.from.url.message"), paramString, null);
			try {
				is = new URL(filepath).openStream();
				pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
			} catch (IOException|SAXException |JAXBException  e) {
				writeLoadError(getText("PMML: Invalid URL",  "mi.text.advf.numeric.pmml.invalid.url.message"), paramString, e);
				pmmlin=null;
		}}else{   	
			writeLoadError(getText("PMML: Loading from file",  "mi.text.advf.numeric.pmml.loading.from.file.message"), paramString, null);
		    pmmlin = new File(filepath);
		    try{
		     is = new FileInputStream(pmmlin);
		   	 pmml = org.jpmml.model.PMMLUtil.unmarshal(is);

		    } catch (IOException|SAXException |JAXBException e ) {
		    	writeLoadError(getText("PMML: Invalid File Path",  "mi.text.advf.numeric.pmml.invalid.filepath.message"), paramString, e);
		}}
	    if (is!=null){ try {is.close();} catch (IOException e) {e.printStackTrace();}}

	if (pmml!=null){
		//Load evaluator to extract needed inputs
		ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
		Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
		evaluator.verify();

		List<InputField> inputFields = evaluator.getInputFields();
		this.numfields = inputFields.size();
		int fcount=0;
		
		//Rename user input parameters based on PMML input fields
		for(InputField inputField : inputFields){
			FieldName name = inputField.getName();
			this.plist.get(fcount).setDisplayName(name.toString());
			fcount++;
	    }
		//Add output fields to dropdown list
	    List<OutputField> outputFields = evaluator.getOutputFields();
	    for(OutputField outputField : outputFields){
	    	String outputname = outputField.getName().toString();
	    	this.outputselect.addOption(outputname);
	    }
    	List<TargetField> targetFields = evaluator.getTargetFields();
	    for(TargetField targetField : targetFields){
	    	FieldName tf=targetField.getName();
	    	//Not all target fields have names
	    	String outputname = "Default Target";
	    	if (tf!=null){
	    		outputname = tf.toString();
	    	}
	    	this.outputselect.addOption(outputname);
	    }
    	
		this.loaded=true;
	}
	}else{
		//Reset button, "unload" the pmmml which will hide all parameters then clear dropdown
		if (((String) getParameterValue("Load")).length() < 3){
			this.outputselect.setOptions(null);
			this.loaded=false;
		}
	}
	  //hide or display output field prompt 
	 if ("outputfield".equals(paramString)){
		 if (this.loaded==false){
			 
			return false;
		 }else{
			return true;
		 }
	 }
	 //hide or display column prompts depending on number of input fields
	 for (int i=1;i<MAX_ALLOWED_FIELDS_IN_MODEL+1;i++){
		  if (this.numfields<=MAX_ALLOWED_FIELDS_IN_MODEL) {
			  String ukey = "p"+i;
			  if (ukey.equals(paramString))
			    {
				  if (this.numfields>=i){
			        return true;
			      }
			      return false;
			    }
		  } else {
			  return false;
		  }
		  
	  }

	  return true;
	  
  }
  public void writeLoadError(String message, String paramString, Exception e){
	  if (paramString.equals("p1")){
		  if (e==null){
			  log.warn(message);
		  }else{
			  log.warn(message, e);
		  }
		 }
  }
  
  public static String getText(String englishText, String translationKey) {

	    String text = UtilString.getResourceString(translationKey);
	    if (text==null) return englishText;
	    return text;

	}
}
