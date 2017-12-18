package pmml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;


import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;
import org.xml.sax.SAXException;

import com.hof.mi.etl.data.ETLStepMetadataFieldBean;
import com.hof.mi.etl.step.AbstractETLStep;
import com.hof.mi.etl.step.ETLStep;
import com.hof.mi.models.report.CustomValue;
import com.hof.parameters.CssDeclarationImpl;
import com.hof.parameters.CssRule;
import com.hof.parameters.CssRuleImpl;
import com.hof.parameters.FileUploadParameter;
import com.hof.parameters.GeneralPanelOptions;
import com.hof.parameters.InputType;
import com.hof.parameters.ListOptions;
import com.hof.parameters.Parameter;
import com.hof.parameters.ParameterDisplayRule;
import com.hof.parameters.ParameterImpl;
import com.hof.parameters.ParameterSection;
import com.hof.parameters.ParameterValidation;
import com.hof.parameters.PropertyLocation;
import com.hof.parameters.ValueDependent;
import com.hof.util.BinaryClassLoaderCache;
import com.hof.util.BinaryClassLoaderCache.InterfaceEntry;
import com.hof.util.Const;
import com.hof.util.UtilString;



public class PMMLMatchSection extends ParameterSection {
	private Evaluator evaluator;
	private List<String> fieldNames;
	private List<String> fieldUUIDs;
	private ETLStep step=null;
	private Map<String, Object> data = null;
	public PMMLMatchSection(List<String> fieldNames, List<String> fieldUUIDs , Evaluator evaluator) {
		this.fieldNames = fieldNames;
		this.fieldUUIDs = fieldUUIDs;

		this.evaluator = evaluator;
		this.data = new HashMap<>();
		this.setDynamicKey("matchSection");
	}
	public PMMLMatchSection(List<String> fieldNames, List<String> fieldUUIDs , Evaluator evaluator, ETLStep step) {
		this.fieldNames = fieldNames;
		this.fieldUUIDs = fieldUUIDs;
		this.step=step;
		this.evaluator = evaluator;
		this.data = new HashMap<>();
		this.setDynamicKey("matchSection");
	}
	
	
	@Override
	public String getSectionKey() {
		return "pmmlMatchSection";
	}

	public String getName() {
		return PMMLStep.getText("Configure Model Input", "mi.text.transformation.step.pmml.configure.input.name");
	}

	public List<Parameter> getParameters() {
		List<Parameter> pList = new ArrayList<Parameter>();


		
		ParameterImpl p = new ParameterImpl();

		List<String> pmmlFields = getPMMLFields(this.evaluator);
		for (String fieldName : pmmlFields) {
			p = new ParameterImpl();
			p.setName(fieldName);
			p.setProperty("FieldMatch"+fieldName);
			p.setInputType(InputType.SELECT);
			p.addViewOption("width", "150px");
			p.setPossibleValues(getFieldMatch());
			ParameterValidation validate = new ParameterValidation();
			validate.setNotEmpty(true);
			p.setValidationRules(validate );
			pList.add(p);	
		}

		return pList;
	}
	public List<CustomValue<?>> getFieldMatch(){
		LinkedList fieldMatches = new LinkedList();
		fieldMatches.add(getEmptySelectorValue());
		for (int i=0;i<fieldNames.size();i++) {
			fieldMatches.add(new CustomValue(fieldUUIDs.get(i).toString(),fieldNames.get(i).toString()));
		}
		return fieldMatches;
	}
	public List<String> getPMMLFields(Evaluator evaluator){
		List<String> pmmlFields = new ArrayList<>();
		List<InputField> inputFields = evaluator.getInputFields();
		for(InputField inputField : inputFields){
			pmmlFields.add(inputField.getName().toString());
		}
		return pmmlFields;
	}	

	@Override
	public List<ParameterDisplayRule> getDisplayRules() {
		return null;
	}
	public CustomValue<?> getEmptySelectorValue()
	{
	  return new CustomValue("", " - - " + PMMLStep.getText("Select", "mi.text.select") + " - - ");
	}
	@Override
	public GeneralPanelOptions getSectionOptions() {
			GeneralPanelOptions gpo = new GeneralPanelOptions();
			gpo.setShowName(true);
			gpo.setCssRules(getParameterSectionCSS());
			return gpo;
	}

	@Override
	public Map<String, ?> getData() {
		if (step!=null){
			AbstractETLStep step = (AbstractETLStep)this.step;
			for(Parameter p: this.getParameters()) {
				data.put(p.getProperty(), convertParameterToJSON(step.getStepOption(p.getProperty())));
			}
			return data;
		}
		else{
			return null;
		}
	}


	@Override
	public String getParameterSectionClassName()
	{
		return "pmmlMatchSection";
	}
	
	public Set<CssRule> getParameterSectionCSS()
	{
		Set<CssRule> cssRules = new HashSet<CssRule>();
		CssRule css =new CssRuleImpl(null, false);
		css.addDeclaration(new CssDeclarationImpl("margin-left", "10px"));
		css.addDeclaration(new CssDeclarationImpl("margin-right", "10px"));
		/*css.addDeclaration(new CssDeclarationImpl("position", "absolute"));
		css.addDeclaration(new CssDeclarationImpl("left", "0"));
		css.addDeclaration(new CssDeclarationImpl("top", "0"));
		css.addDeclaration(new CssDeclarationImpl("bottom", "65px"));
		css.addDeclaration(new CssDeclarationImpl("right", "0"));
		css.addDeclaration(new CssDeclarationImpl("overflow-y", "auto"));
		css.addDeclaration(new CssDeclarationImpl("overflow-x", "none"));*/
		
		cssRules.add(css);
		return cssRules;
	}

	
}
