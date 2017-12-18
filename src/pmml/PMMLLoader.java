package pmml;

import com.hof.mi.models.report.CustomValue;
import com.hof.parameters.ParameterSection;
import com.hof.parameters.ParameterValueLoader;
import com.hof.util.YFLogger;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.PMMLUtil;
import org.xml.sax.SAXException;

public class PMMLLoader
  extends ParameterValueLoader
{
  private static final YFLogger log = YFLogger.getLogger(PMMLStep.class.getName());
  List<String> errors = new ArrayList<String>();
  public void generateDynamicParameters()
    throws Exception
  {
  	this.response.addKeyToRemove("errorSection");
    this.response.addKeyToRemove("outputSection");
    this.response.addKeyToRemove("matchSection");
    Evaluator evaluator = null;
    if ((eventData.get("fileUpload")==null || "null".equals(eventData.get("fileUpload"))) && (eventData.get("pmmlFilePath")==null || "null".equals(eventData.get("pmmlFilePath")))) {
    	errors.add(PMMLStep.getText("No filepath or URL specified", "mi.text.transformation.step.pmml.error2.message"));
    }
    else if (this.events.contains("fileUploaded") && eventData.get("fileUpload")!=null && !"null".equals(eventData.get("fileUpload")))
    {
      Integer fileId = Integer.valueOf(this.eventData.get("fileUpload").toString());
      evaluator = getEvaluatorFromFile(getFile(fileId));
      if(evaluator == null) {
    	  errors.add(PMMLStep.getText("The file you selected is not a valid PMML file.", "mi.text.transformation.step.pmml.error1.message"));  
      }
    }
    else if ((this.events.contains("pathSet")) && (this.eventData.get("pmmlFilePath") != null  && !"null".equals(eventData.get("pmmlFilePath")))) {
      evaluator = getEvaluatorFromPath(this.eventData.get("pmmlFilePath").toString());
    }
    if (evaluator != null)
    {
      List<String> fieldNames = (List)this.eventData.get("fieldNames");
      List<String> fieldUUIDs = (List)this.eventData.get("fieldUUIDs");
      
      ///
      ParameterSection matchSection = new PMMLMatchSection(fieldNames, fieldUUIDs, evaluator);
      this.response.addSection("matchSection", "pmmlPanel", matchSection);
      
      ParameterSection outputSection = new PMMLOutputSection(evaluator);
      this.response.addSection("outputSection", "pmmlPanel", outputSection);
    }
    else
    {
      ParameterSection errormatchSection = new PMMLErrorSection(errors);
      this.response.addSection("errorSection", "pmmlPanel", errormatchSection);
    }
  }
  
  public Map<String, List<CustomValue<?>>> getUpdatedPossibleValues()
    throws Exception
  {
    Map<String, List<CustomValue<?>>> updatedVals = new HashMap();
    return updatedVals;
  }
  
  public Evaluator getEvaluatorFromFile(byte[] file)
  {
    PMML pmml = null;
    if (file != null)
    {
      try
      {
        InputStream is = new ByteArrayInputStream(file);
        pmml = PMMLUtil.unmarshal(is);
      }
      catch (SAXException|JAXBException e)
      {
        return null;
      }
      ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
      Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
      evaluator.verify();
      return evaluator;
    }
    return null;
  }
  
  public Evaluator getEvaluatorFromPath(String filepath)
  {
    PMML pmml = null;
    if (filepath == null || filepath.equals("") || "null".equals(filepath)){
    	errors.add(PMMLStep.getText("No filepath or URL specified", "mi.text.transformation.step.pmml.error2.message"));
    	return null;
    }
    if (filepath != null)
    {
		InputStream is;
		if (filepath.contains("http:")||filepath.contains("https:")) {
			try {
				URL url = new URL(filepath);
				URLConnection conn = url.openConnection();
				conn.connect();
				if (conn.getContentLength()>0 && conn.getContentType().contains("/xml")){
					is= conn.getInputStream();
					pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
				}else{
					errors.add(PMMLStep.getText("Unable to load PMML from URL", "mi.text.transformation.step.pmml.error3.message"));
					return null;
				}
			} catch (Exception e) {
				log.error("Unable to load PMML from URL",e);
				errors.add(PMMLStep.getText("Unable to load PMML from URL", "mi.text.transformation.step.pmml.error3.message"));
				return null;
			}
		}else{   	
			try {
				is = new FileInputStream(filepath);
			} catch (FileNotFoundException e) {
				errors.add(PMMLStep.getText("File not found", "mi.text.transformation.step.pmml.error4.message"));
				return null;
			}
			try {
				pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
			} catch (SAXException | JAXBException e) {
				errors.add(PMMLStep.getText("Unable to load PMML from file", "mi.text.transformation.step.pmml.error3.message"));
				log.error("Unable to load PMML from file",e);
				return null;
			}
		} 
		ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
		Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
		evaluator.verify();
		return evaluator;
    }
    return null;
  }
}
