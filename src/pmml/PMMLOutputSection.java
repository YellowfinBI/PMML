package pmml;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dmg.pmml.FieldName;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;

import com.hof.mi.etl.step.AbstractETLStep;
import com.hof.mi.etl.step.ETLStep;
import com.hof.mi.models.report.CustomValue;
import com.hof.parameters.CssDeclarationImpl;
import com.hof.parameters.CssRule;
import com.hof.parameters.CssRuleImpl;
import com.hof.parameters.GeneralPanelOptions;
import com.hof.parameters.InputType;
import com.hof.parameters.Parameter;
import com.hof.parameters.ParameterDisplayRule;
import com.hof.parameters.ParameterImpl;
import com.hof.parameters.ParameterSection;
import com.hof.parameters.ParameterValidation;
import com.hof.util.UtilString;



public class PMMLOutputSection extends ParameterSection {
	private Evaluator evaluator;


	private Map<String, Object> data = null;


	private ETLStep step=null;
	public PMMLOutputSection(Evaluator evaluator) {

		this.evaluator = evaluator;
		this.data = new HashMap<>();
		this.setDynamicKey("outputSection");

	}
	public PMMLOutputSection(Evaluator evaluator, ETLStep step) {
		this.step=step;
		this.evaluator = evaluator;
		this.data = new HashMap<>();
		this.setDynamicKey("outputSection");

	}
	@Override
	public String getSectionKey() {
		return "pmmlOutputSection";
	}

	public String getName() {
		return PMMLStep.getText("Configure Model Output", "mi.text.transformation.step.pmml.configure.output.name");
	}

	public List<Parameter> getParameters() {
		List<Parameter> pList = new ArrayList<Parameter>();


		
		ParameterImpl p = new ParameterImpl();

		p = new ParameterImpl();
		p.setName(PMMLStep.getText("Fields to Extract", "mi.text.transformation.step.pmml.fields.to.extract"));
		p.setProperty("pmmlOutput");
		p.setInputType(InputType.CHECKBOX);
		p.setPossibleValues(getPMMLOutput(this.evaluator));
		ParameterValidation valid =  new ParameterValidation();
		valid.setNotEmpty(true);
		p.setValidationRules(valid);
		p.setCssRules(getCheckboxCSS());
		pList.add(p);

		return pList;
	}

	public List<CustomValue<?>> getPMMLOutput(Evaluator evaluator){
		LinkedList pmmlOutputs = new LinkedList();
		if (evaluator==null) return pmmlOutputs;
		List<OutputField> outputFields = evaluator.getOutputFields();
		for(OutputField outputField : outputFields){
			pmmlOutputs.add(new CustomValue(outputField.getName().toString(),outputField.getName().toString()));
		}
		List<TargetField> targetFields = evaluator.getTargetFields();
		for(TargetField targetField : targetFields){
		   FieldName tf=targetField.getName();
		    	String outputname = "DefaultTarget";
		    	if (tf!=null){
		    		outputname = tf.toString();
		    	}
		    	pmmlOutputs.add(new CustomValue(outputname,"Default Target"));
		}
		return pmmlOutputs;
	}
	@Override
	public List<ParameterDisplayRule> getDisplayRules() {
		return null;
	}
	public CustomValue<?> getEmptySelectorValue()
	{
	  return new CustomValue("", " - - " + UtilString.getResourceString("mi.text.select") + " - - ");
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
		return "pmmlOutputSection";
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


	
	public Set<CssRule> getCheckboxCSS()
	{
		Set<CssRule> cssRules = new HashSet<CssRule>();
		CssRule css =new CssRuleImpl("img", false);
		css.addDeclaration(new CssDeclarationImpl("top", "5px"));
		
		cssRules.add(css);
		return cssRules;
	}

	
}
