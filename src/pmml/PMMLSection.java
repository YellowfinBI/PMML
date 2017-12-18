package pmml;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hof.mi.etl.step.AbstractETLStep;
import com.hof.parameters.CssDeclarationImpl;
import com.hof.parameters.CssRule;
import com.hof.parameters.CssRuleImpl;
import com.hof.parameters.FileUploadParameter;
import com.hof.parameters.GeneralPanelOptions;
import com.hof.parameters.InputType;
import com.hof.parameters.Parameter;
import com.hof.parameters.ParameterDisplayRule;
import com.hof.parameters.ParameterImpl;
import com.hof.parameters.ParameterSection;
import com.hof.parameters.ParameterValidation;
import com.hof.parameters.PropertyLocation;



public class PMMLSection extends ParameterSection {

	private AbstractETLStep step =null;
	private Map<String, Object> data = null;
	private PMMLLoader pmmlLoader = new PMMLLoader();
	public PMMLSection(AbstractETLStep step) {
		this.step = step;
		data = new HashMap<>();
		this.parameterValueLoader=pmmlLoader;
		this.setParameterValueLoader(pmmlLoader);
	}
	
	@Override
	public String getSectionKey() {
		return "pmmlSection";
	}

	public String getName() {
		return PMMLStep.getText("Load PMML File", "mi.text.transformation.step.pmml.section.name");
	}

	public List<Parameter> getParameters() {
		List<Parameter> pList = new ArrayList<Parameter>();

		ParameterValidation validation = new ParameterValidation();
	    validation.setNotEmpty(true);
	      
	    
		ParameterImpl p = new ParameterImpl();
		p.setName(PMMLStep.getText("Load from Path", "mi.text.transformation.step.pmml.load.from.path"));
		p.setProperty("loadFromPath");
		p.setInputType(InputType.TOGGLE);
		p.setDefaultValue(Boolean.valueOf(false));
		pList.add(p);
		
		List<String> fieldUUIDs = new ArrayList<String>();
		List<String> fieldNames = new ArrayList<String>();
		Map<String, String> options = step.getInputToDefaultFieldMap();

		for (String key : options.keySet()){
			if (options.get(key)!=null && key!=null){
				if (!key.equals(options.get(key))){
					fieldUUIDs.add(options.get(key));
					fieldNames.add(step.getDefaultMetadataFieldsMap().get(options.get(key)).getFieldName());
				}
			}
		}
		
		p = new FileUploadParameter("fileUpload", ".xml", "ETLSTEP", "fileUploaded");
		p.addEventData("fieldNames", fieldNames);
		p.addEventData("fieldUUIDs", fieldUUIDs);
		p.addEventData("stepName", step.getETLStepBean().getStepName());
	    p.addViewOption("text", PMMLStep.getText("Drag PMML File Here (.xml)", "mi.text.transformation.step.pmml.drag.file"));
	    p.addDisplayRule(new ParameterDisplayRule("AND","loadFromPath",Boolean.valueOf(false),false));
	    pList.add(p);
		
		p = new ParameterImpl();
		p.setName(PMMLStep.getText("Complete Path/URL", "mi.text.transformation.step.pmml.complete.path"));
		p.setProperty("pmmlFilePath");
	    p.addViewOption("width", "365px");
		p.setInputType(InputType.TEXTBOX);
		p.addDisplayRule(new ParameterDisplayRule("AND","loadFromPath",Boolean.valueOf(true),false));
		p.setValidationRules(validation);
		pList.add(p);
		
		p = new ParameterImpl();
		p.setInputType(InputType.BUTTON);
		p.setEvent("pathSet");
	    p.addViewOption("text", PMMLStep.getText("Load File", "mi.text.transformation.step.pmml.load.file"));
		p.addEventData("fieldNames", fieldNames);
		p.addEventData("fieldUUIDs", fieldUUIDs);
	    p.addViewOption("width", "375px");
		p.addEventData("stepName", step.getETLStepBean().getStepName());
		p.addEventParameter("pmmlFilePath", new PropertyLocation("pmmlPanel", "pmmlSection"));
		p.addDisplayRule(new ParameterDisplayRule("AND","loadFromPath",Boolean.valueOf(true),false));
		pList.add(p);
		

		return pList;
	}

	@Override
	public List<ParameterDisplayRule> getDisplayRules() {
		return null;
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
		AbstractETLStep step = (AbstractETLStep)this.step;
		for(Parameter p: this.getParameters()) {
			data.put(p.getProperty(), convertParameterToJSON(step.getStepOption(p.getProperty())));
		}

		return data;
	}
	
	public void addData(String property, Object value) {
		data.put(property, value);
	}

	@Override
	public String getParameterSectionClassName()
	{
		return "pmmlSection";
	}
	
	public Set<CssRule> getParameterSectionCSS()
	{
		Set<CssRule> cssRules = new HashSet<CssRule>();
		CssRule css =new CssRuleImpl(null, false);
		css.addDeclaration(new CssDeclarationImpl("margin-left", "10px"));
		css.addDeclaration(new CssDeclarationImpl("margin-right", "10px"));
		
		cssRules.add(css);
		return cssRules;
	}
	
}
