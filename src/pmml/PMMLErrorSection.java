package pmml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hof.parameters.CssDeclarationImpl;
import com.hof.parameters.CssRule;
import com.hof.parameters.CssRuleImpl;
import com.hof.parameters.GeneralPanelOptions;
import com.hof.parameters.InputType;
import com.hof.parameters.Parameter;
import com.hof.parameters.ParameterDisplayRule;
import com.hof.parameters.ParameterImpl;
import com.hof.parameters.ParameterSection;

public class PMMLErrorSection extends ParameterSection{

	List<String> messages;
	public PMMLErrorSection(List<String> m) {
		this.messages = m;
		this.setDynamicKey("errorSection");
	}
	
	
	@Override
	public String getSectionKey() {
		// TODO Auto-generated method stub
		return "pmmlErrorSection";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return PMMLStep.getText("Errors", "mi.text.transformation.step.pmml.section.errors.name");
	}

	@Override
	public List<Parameter> getParameters() {
		
		List<Parameter> parameters = new ArrayList<Parameter>();
		for(String m:messages) {
			ParameterImpl p = new ParameterImpl();
	          p.setInputType(InputType.STATIC);
	          p.setProperty("test");
	          p.addViewOption("text", "<div style=\"color:red\">"+m+"</div>");
	          parameters.add(p);
		}
		return parameters;
	}

	@Override
	public List<ParameterDisplayRule> getDisplayRules() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getParameterSectionClassName()
	{
		return "pmmlErrorSection";
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
