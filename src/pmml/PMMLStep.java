package pmml;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;

import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.InvalidResultException;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.ResultField;
import org.jpmml.evaluator.TargetField;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hof.mi.data.Wire;
import com.hof.mi.etl.ETLElement;
import com.hof.mi.etl.ETLException;
import com.hof.mi.etl.ETLStepCategory;
import com.hof.mi.etl.data.ETLStepMetadataFieldBean;
import com.hof.mi.etl.step.AbstractETLRowStep;
import com.hof.mi.etl.step.ETLStepAPIVersion;
import com.hof.mi.etl.step.definition.ui.ETLStepConfigPanel;
import com.hof.mi.etl.step.definition.ui.ETLStepPanels;
import com.hof.parameters.GeneralPanelOptions;
import com.hof.parameters.ParameterPanelCollection;
import com.hof.parameters.ParameterSection;
import com.hof.util.Const;
import com.hof.util.UtilString;
import com.hof.util.YFLogger;

public class PMMLStep extends AbstractETLRowStep {
	public int counter = 0;
	public Evaluator evaluator;
	private static final YFLogger log = YFLogger.getLogger(PMMLStep.class.getName());
	@Override
	public String getDefaultName() {
		return getText("PMML Model Prediction", "mi.text.transformation.step.pmml.name");
	}

	@Override
	public String getDefaultDescription() {
		return getText("Run predictions against a model stored as a PMML file.", "mi.text.transformation.step.pmml.default.description");
	}
	  
	@Override
	public ETLStepCategory getStepCategory() {
		return ETLStepCategory.TRANSFORM;
	}
	@Override
	public Collection<ETLException> validate() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override 
	public String getOffImage() {
		return "PHN2ZyBpZD0iTGF5ZXJfMSIgZGF0YS1uYW1lPSJMYXllciAxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0MCA0MCI+CiAgPGRlZnM+CiAgICA8c3R5bGU+CiAgICAgIC5jbHMtMSwgLmNscy0zIHsKICAgICAgICBmaWxsOiAjYmJiOwogICAgICB9CgogICAgICAuY2xzLTIg"+
				"ewogICAgICAgIG9wYWNpdHk6IDAuNTsKICAgICAgfQoKICAgICAgLmNscy0zIHsKICAgICAgICBvcGFjaXR5OiAwLjI1OwogICAgICB9CiAgICA8L3N0eWxlPgogIDwvZGVmcz4KICA8dGl0bGU+QXJ0Ym9hcmQgMTwvdGl0bGU+CiAgPGc+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik0yMCw5LjgzMzMzYy0uMDU4"+
				"MTMsMC0uMTE2MjYuMDA2ODgtLjE3NDM4LjAwODEzLjExNjI2LS4wMDUuMjMyNTEtLjAwODEzLjM1MTI3LS4wMDgxM1oiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMSIgZD0iTTE5LjgyNTU1LDkuODQxNjVjLS4xMTUuMDAzNzUtLjIyOTM5LjAwODc1LS4zNDI1Mi4wMTY4OC4xMTEyNi0uMDExODcuMjI3NTEtLjAxMzEz"+
				"LjM0MjUyLS4wMTY4OCIvPgogICAgPGcgY2xhc3M9ImNscy0yIj4KICAgICAgPHBhdGggY2xhc3M9ImNscy0xIiBkPSJNMTAuMDA2LDIwLjAwMTc1QTEwLjE2OCwxMC4xNjgsMCwwLDAsMjAuMDU4NDQsMzAuMTY2NjdhNS4wODI2MSw1LjA4MjYxLDAsMCwwLS4wNTgxMy0xMC4xNjQ5Miw1LjA4NSw1LjA4NSwwLDAsMS0u"+
				"NTIwNjUtMTAuMTQzLDEwLjE2ODMxLDEwLjE2ODMxLDAsMCwwLTkuNDczNjMsMTAuMTQzIi8+CiAgICA8L2c+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTMiIGQ9Ik0yOS45OTQsMjAuMDAxNzVBMTAuMTY3NjYsMTAuMTY3NjYsMCwwLDAsMTkuOTQxNTYsOS44MzQzM2E1LjA4Mzg4LDUuMDgzODgsMCwwLDAsLjA1ODEzLDEw"+
				"LjE2NzQyQTUuMDg0NTMsNS4wODQ1MywwLDAsMSwyMC41MjEsMzAuMTQ0MTZhMTAuMTY3MTMsMTAuMTY3MTMsMCwwLDAsOS40NzMtMTAuMTQyNDEiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMSIgZD0iTTE5LjgyNTU1LDkuODMzMzNjLjAzOTM4LDAsLjA3Njg4LjAwMTI1LjExNjI2LjAwMTI1LjAxODc1LDAsLjAzOTM4"+
				"LS4wMDEyNS4wNTgxMy0uMDAxMjVaIi8+CiAgPC9nPgogIDxwYXRoIGNsYXNzPSJjbHMtMSIgZD0iTTMzLjExMjg1LDE2LjU4MzMzbC0xLjE2NzQxLDUuNTcwMThoMy4zMDE3OGwtLjI2ODA3LDEuMjYzMTVIMzAuMTc0ODdsMS40MjU5LTYuODMzMzNabS04LjEwNjA2LDAsLjUyNjU2LDQuOTQ3ODhoLjAxOTE1bDIuNjMx"+
				"NjEtNC45NDc4OGgyLjA5NjA3bC0xLjQ0NTA1LDYuODMzMzNIMjcuNDI4MzhsMS4yNDQtNS40MzYxNWgtLjAxOTE1bC0yLjg5MDcsNS40MzYxNUgyNC42MTQyNmwtLjY2MDYtNS40MzYxNWgtLjAxOTE1bC0xLjAyMzgsNS40MzYxNUgyMS40OTQzOWwxLjQzNTQ4LTYuODMzMzNabS04LjY4MDQ5LDAsLjUyNiw0Ljk0Nzg4"+
				"aC4wMTkxNWwyLjYzMjIxLTQuOTQ3ODhIMjEuNTk5N2wtMS40NDUwNSw2LjgzMzMzSDE4Ljc0NzI5bDEuMjQ0Ni01LjQzNjE1aC0uMDE5MTVMMTcuMDgyLDIzLjQxNjY3SDE1LjkzMzc4bC0uNjYwNi01LjQzNjE1SDE1LjI1NGwtMS4wMjM4LDUuNDM2MTVIMTIuODEzOWwxLjQzNTQ4LTYuODMzMzNabS02LjA0ODg4LDMu"+
				"MjE1NjJhMS4zMTYwNiwxLjMxNjA2LDAsMCwwLC45MDQ3My0uMjkyLDEuMTQ1NTQsMS4xNDU1NCwwLDAsMCwuMzMwMy0uOTA0MTMuODkuODksMCwwLDAtLjA5MS0uNDI2LjY2OTI5LjY2OTI5LDAsMCwwLS4yNDQxMy0uMjU4NDksMS4xMDA2MiwxLjEwMDYyLDAsMCwwLS4zMzk4Ny0uMTI5MjUsMS45NTQ0OSwxLjk1NDQ5"+
				"LDAsMCwwLS4zODc3NC0uMDM4M0g5LjE2NzQ2bC0uNDIxMjUsMi4wNDgyMVptLjM2MzgxLTMuMjE1NjJhNC4yMjkxNSw0LjIyOTE1LDAsMCwxLC45NTIuMTAwNTMsMi4wMzk1MiwyLjAzOTUyLDAsMCwxLC43NTE1NS4zMzAzLDEuNTQzOCwxLjU0MzgsMCwwLDEsLjQ5MzA1LjYxMjEzLDIuMjU3ODQsMi4yNTc4NCwwLDAs"+
				"MSwuMTc3MTIuOTQ3ODEsMi4yODYxMSwyLjI4NjExLDAsMCwxLS4yMTU0MSwxLjAwOTQ0LDIuMjE4NSwyLjIxODUsMCwwLDEtLjU4NC43NTE1NSwyLjY0ODczLDIuNjQ4NzMsMCwwLDEtLjg0NjY5LjQ2OTEyQTMuMTM5MiwzLjEzOTIsMCwwLDEsMTAuMzU0LDIwLjk2N0g4LjQ5NzI5bC0uNTE3LDIuNDQ5NzFINi40ODcz"+
				"OGwxLjQxNjkzLTYuODMzMzNaIi8+Cjwvc3ZnPgo=";
	}
	
	@Override 
	public String getOnImage() {
		return "PHN2ZyBpZD0iTGF5ZXJfMSIgZGF0YS1uYW1lPSJMYXllciAxIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0MCA0MCI+CiAgPGRlZnM+CiAgICA8c3R5bGU+CiAgICAgIC5jbHMtMSwgLmNscy0zIHsKICAgICAgICBmaWxsOiAjZmZmOwogICAgICB9CgogICAgICAuY2xzLTIg"+
				"ewogICAgICAgIG9wYWNpdHk6IDAuNTsKICAgICAgfQoKICAgICAgLmNscy0zIHsKICAgICAgICBvcGFjaXR5OiAwLjI1OwogICAgICB9CiAgICA8L3N0eWxlPgogIDwvZGVmcz4KICA8dGl0bGU+QXJ0Ym9hcmQgMTwvdGl0bGU+CiAgPGc+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTEiIGQ9Ik0yMCw5LjgzMzMzYy0uMDU4"+
				"MTMsMC0uMTE2MjYuMDA2ODgtLjE3NDM4LjAwODEzLjExNjI2LS4wMDUuMjMyNTEtLjAwODEzLjM1MTI3LS4wMDgxM1oiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMSIgZD0iTTE5LjgyNTU1LDkuODQxNjVjLS4xMTUuMDAzNzUtLjIyOTM5LjAwODc1LS4zNDI1Mi4wMTY4OC4xMTEyNi0uMDExODcuMjI3NTEtLjAxMzEz"+
				"LjM0MjUyLS4wMTY4OCIvPgogICAgPGcgY2xhc3M9ImNscy0yIj4KICAgICAgPHBhdGggY2xhc3M9ImNscy0xIiBkPSJNMTAuMDA2LDIwLjAwMTc1QTEwLjE2OCwxMC4xNjgsMCwwLDAsMjAuMDU4NDQsMzAuMTY2NjdhNS4wODI2MSw1LjA4MjYxLDAsMCwwLS4wNTgxMy0xMC4xNjQ5Miw1LjA4NSw1LjA4NSwwLDAsMS0u"+
				"NTIwNjUtMTAuMTQzLDEwLjE2ODMxLDEwLjE2ODMxLDAsMCwwLTkuNDczNjMsMTAuMTQzIi8+CiAgICA8L2c+CiAgICA8cGF0aCBjbGFzcz0iY2xzLTMiIGQ9Ik0yOS45OTQsMjAuMDAxNzVBMTAuMTY3NjYsMTAuMTY3NjYsMCwwLDAsMTkuOTQxNTYsOS44MzQzM2E1LjA4Mzg4LDUuMDgzODgsMCwwLDAsLjA1ODEzLDEw"+
				"LjE2NzQyQTUuMDg0NTMsNS4wODQ1MywwLDAsMSwyMC41MjEsMzAuMTQ0MTZhMTAuMTY3MTMsMTAuMTY3MTMsMCwwLDAsOS40NzMtMTAuMTQyNDEiLz4KICAgIDxwYXRoIGNsYXNzPSJjbHMtMSIgZD0iTTE5LjgyNTU1LDkuODMzMzNjLjAzOTM4LDAsLjA3Njg4LjAwMTI1LjExNjI2LjAwMTI1LjAxODc1LDAsLjAzOTM4"+
				"LS4wMDEyNS4wNTgxMy0uMDAxMjVaIi8+CiAgPC9nPgogIDxwYXRoIGNsYXNzPSJjbHMtMSIgZD0iTTMzLjExMjg1LDE2LjU4MzMzbC0xLjE2NzQxLDUuNTcwMThoMy4zMDE3OGwtLjI2ODA3LDEuMjYzMTVIMzAuMTc0ODdsMS40MjU5LTYuODMzMzNabS04LjEwNjA2LDAsLjUyNjU2LDQuOTQ3ODhoLjAxOTE1bDIuNjMx"+
				"NjEtNC45NDc4OGgyLjA5NjA3bC0xLjQ0NTA1LDYuODMzMzNIMjcuNDI4MzhsMS4yNDQtNS40MzYxNWgtLjAxOTE1bC0yLjg5MDcsNS40MzYxNUgyNC42MTQyNmwtLjY2MDYtNS40MzYxNWgtLjAxOTE1bC0xLjAyMzgsNS40MzYxNUgyMS40OTQzOWwxLjQzNTQ4LTYuODMzMzNabS04LjY4MDQ5LDAsLjUyNiw0Ljk0Nzg4"+
				"aC4wMTkxNWwyLjYzMjIxLTQuOTQ3ODhIMjEuNTk5N2wtMS40NDUwNSw2LjgzMzMzSDE4Ljc0NzI5bDEuMjQ0Ni01LjQzNjE1aC0uMDE5MTVMMTcuMDgyLDIzLjQxNjY3SDE1LjkzMzc4bC0uNjYwNi01LjQzNjE1SDE1LjI1NGwtMS4wMjM4LDUuNDM2MTVIMTIuODEzOWwxLjQzNTQ4LTYuODMzMzNabS02LjA0ODg4LDMu"+
				"MjE1NjJhMS4zMTYwNiwxLjMxNjA2LDAsMCwwLC45MDQ3My0uMjkyLDEuMTQ1NTQsMS4xNDU1NCwwLDAsMCwuMzMwMy0uOTA0MTMuODkuODksMCwwLDAtLjA5MS0uNDI2LjY2OTI5LjY2OTI5LDAsMCwwLS4yNDQxMy0uMjU4NDksMS4xMDA2MiwxLjEwMDYyLDAsMCwwLS4zMzk4Ny0uMTI5MjUsMS45NTQ0OSwxLjk1NDQ5"+
				"LDAsMCwwLS4zODc3NC0uMDM4M0g5LjE2NzQ2bC0uNDIxMjUsMi4wNDgyMVptLjM2MzgxLTMuMjE1NjJhNC4yMjkxNSw0LjIyOTE1LDAsMCwxLC45NTIuMTAwNTMsMi4wMzk1MiwyLjAzOTUyLDAsMCwxLC43NTE1NS4zMzAzLDEuNTQzOCwxLjU0MzgsMCwwLDEsLjQ5MzA1LjYxMjEzLDIuMjU3ODQsMi4yNTc4NCwwLDAs"+
				"MSwuMTc3MTIuOTQ3ODEsMi4yODYxMSwyLjI4NjExLDAsMCwxLS4yMTU0MSwxLjAwOTQ0LDIuMjE4NSwyLjIxODUsMCwwLDEtLjU4NC43NTE1NSwyLjY0ODczLDIuNjQ4NzMsMCwwLDEtLjg0NjY5LjQ2OTEyQTMuMTM5MiwzLjEzOTIsMCwwLDEsMTAuMzU0LDIwLjk2N0g4LjQ5NzI5bC0uNTE3LDIuNDQ5NzFINi40ODcz"+
				"OGwxLjQxNjkzLTYuODMzMzNaIi8+Cjwvc3ZnPgo=";}
	
	
	@Override

	  public String getOnImageMIMEType()
	  {
	    return Const.MIME_TYPE_SVG;
	  }
	@Override
	  
	  public String getOffImageMIMEType()
	  {
	    return Const.MIME_TYPE_SVG;
	  }
	@Override
	protected ParameterPanelCollection generatePanelCollection() {
		ETLStepConfigPanel pmmlConfigPanel = new ETLStepConfigPanel("pmmlPanel",getETLStepBean().getStepName());
		GeneralPanelOptions sectionOptions=  new GeneralPanelOptions();
		PMMLSection pmmlSection = new PMMLSection(this);
		String documentId = this.getStepOption("fileUpload") == null ? "" : this.getStepOption("fileUpload");
		pmmlSection.addData("stepUUID", this.getUuid());
		pmmlSection.addData("fileUpload", documentId);
		pmmlConfigPanel.addSection(pmmlSection);
		
		
		List<String> fieldUUIDs = new ArrayList<String>();
		List<String> fieldNames = new ArrayList<String>();
		
		Map<String, String> options = getInputToDefaultFieldMap();

		for (String key : options.keySet()){
			if (options.get(key)!=null && key!=null){
				if (!key.equals(options.get(key))){
					fieldUUIDs.add(options.get(key));
					fieldNames.add(getDefaultMetadataFieldsMap().get(options.get(key)).getFieldName());
				}
			}
		}
		
		setEvaluator();
		if (this.evaluator!=null){
			ParameterSection matchSection = new PMMLMatchSection(fieldNames,fieldUUIDs, evaluator,this);			
			ParameterSection outputSection = new PMMLOutputSection(evaluator,this);
			pmmlConfigPanel.addSection(matchSection);
			pmmlConfigPanel.addSection(outputSection);

		}
		
		
		
		HashMap saveOptions = new HashMap();
		saveOptions.put("flat", Boolean.valueOf(true));
		saveOptions.put("cssClass", "configure-apply-btn");
		
		GeneralPanelOptions panelOptions=  new GeneralPanelOptions();
		panelOptions.setSaveButton(true);
		panelOptions.setSaveButtonOptions(saveOptions);
		panelOptions.setSaveText(getText("Save", "mi.text.transformation.step.pmml.save"));
		
		
		
		pmmlConfigPanel.setGeneralOptions(panelOptions);
		ETLStepPanels pmmlPanelCollection = new ETLStepPanels();
		pmmlPanelCollection.addPanel(pmmlConfigPanel);
		
		return pmmlPanelCollection;
		
	}

	@Override
	public void setupGeneratedFields() {
		setEvaluator();
		Map<String, String> options = getInputToDefaultFieldMap();
		Collection<String> previousUUIDs = new ArrayList<>();
		for (String key : options.keySet()) {
			if (!key.equals(options.get(key))) {
				previousUUIDs.add(options.get(key));
			}
		}
		List<String> selectedFields = getOutputs();
		List<ETLStepMetadataFieldBean> defaultFields = getDefaultMetadataFields();
		
		if (evaluator!= null) {
			List<OutputField> outputFields = evaluator.getOutputFields();
			if (outputFields != null) {
				List<String> outputFieldsUUID = new ArrayList<String>();
				for (OutputField outputField : outputFields) {
					String outUUID = getStepOption(outputField.getName()+"Selected");
					outputFieldsUUID.add(outUUID);
				}
				

				for (ETLStepMetadataFieldBean bean : defaultFields) {
					if (bean.getLinkFieldUUID().equals(bean.getEtlStepMetadataFieldUUID()) 
							&& !outputFieldsUUID.contains(bean.getEtlStepMetadataFieldUUID())) {
						removeDefaultMetadataField(bean.getEtlStepMetadataFieldUUID());
					}
				}
			}
		}
		
		if (defaultFields != null && defaultFields.size() > 0 && evaluator != null) {
			List<OutputField> outputFields = evaluator.getOutputFields();
			List<TargetField> targetFields = evaluator.getTargetFields();
			for (OutputField outputField : outputFields) {
				setupGeneratedField(outputField, null, selectedFields, defaultFields.get(0).duplicate());
			}
			for (TargetField targetField : targetFields) {
				setupGeneratedField(targetField, "DefaultTarget", selectedFields, defaultFields.get(0).duplicate());
			}
		}
	}

	private void setupGeneratedField(ResultField outputField, String defaultFieldName, List<String> selectedFields, ETLStepMetadataFieldBean templateField) {
		String outputFieldName = null;
		FieldName outputFieldNameObj = outputField.getName();
		if (outputFieldNameObj == null || UtilString.isNullOrEmpty(outputFieldNameObj.toString())) {
			outputFieldName = defaultFieldName;
		} else {
			outputFieldName = outputFieldNameObj.toString();
		}
		
		String outUUID = getStepOption(outputFieldName+"Selected");
		if (selectedFields.contains(outputFieldName)) {
			ETLStepMetadataFieldBean mData = getDefaultMetadataFieldsMap().get(outUUID);
			if (outUUID != null && mData != null) {
				if (outputField.getDataType() != null) {
					if (outputField.getDataType().equals(DataType.DOUBLE)
							|| outputField.getDataType().equals(DataType.FLOAT)
							|| outputField.getDataType().equals(DataType.INTEGER)) {
						mData.setFieldType("NUMERIC");
					} else {
						mData.setFieldType("TEXT");
					}
				} else {
					mData.setFieldType("TEXT");
				}
					
			} else {
				ETLStepMetadataFieldBean newField = templateField.duplicate();
				newField.setFieldName(outputFieldName);
				if (outputField.getDataType() != null) {
					if (outputField.getDataType().equals(DataType.DOUBLE)
							|| outputField.getDataType().equals(DataType.FLOAT)
							|| outputField.getDataType().equals(DataType.INTEGER)) {
						newField.setFieldType("NUMERIC");
					} else {
						newField.setFieldType("TEXT");
					}
				} else {
					newField.setFieldType("TEXT");
				}
				addNewGeneratedField(newField, outputFieldName+"Selected");
			}
		} else if (!UtilString.isNullOrEmpty(outUUID)) {
			// The model output field is not selected but there is a default metadata field. Remove this.
			removeDefaultMetadataField(outUUID);
		}
	}
	
	@Override
	public Map<String, String> getDefaultInternalOptions() {
		return null;
	}

	public void setEvaluator(){
		if (this.evaluator==null){
			if ((getStepOption("fileUpload")==null || "null".equals(getStepOption("fileUpload"))) && getStepOption("pmmlFilePath")==null){
				return;
			}
			if(!Boolean.valueOf(getStepOption("loadFromPath"))) {
				if(getStepOption("fileUpload")!=null && !"null".equals(getStepOption("fileUpload"))){
					Integer fileId = Integer.valueOf(getStepOption("fileUpload"));
					this.evaluator=getEvaluatorFromFile(this.getFile(fileId));
				}
			} 
			else{ 
				this.evaluator=getEvaluatorFromPath(getStepOption("pmmlFilePath"));
			} 
		}
	}
	@Override
	public Map<String, String> getValidatedStepOptions() {
		return getStepOptions();
	}

	@Override
	protected boolean processWireData(List<ETLStepMetadataFieldBean> fields) throws ETLException, InterruptedException {
		setEvaluator();

		if (this.evaluator!=null){
			List<InputField> inputFields = this.evaluator.getInputFields();

			Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
			for (InputField PMMLField : inputFields){
				String tempUUID = getStepOption("FieldMatch"+PMMLField.getName().toString());
				Wire<Object,String> resultWire = getWireForField(tempUUID);
				if (resultWire!=null && resultWire.getValue()!=null) { 
					if (!resultWire.getValue().toString().equals("")) {
					try{
						arguments.put(PMMLField.getName(), PMMLField.prepare(resultWire.getValue().toString()));
					}catch (InvalidResultException e){
						throw new ETLException(ETLElement.STEP, getUuid(), null, getText("Invalid Input to field ", "mi.text.transformation.step.pmml.invalid.input")+PMMLField.getName()+": "+ resultWire.getValue(), null);			
				}}}
			}
			Map<FieldName, ?> results = this.evaluator.evaluate(arguments);
				//Get the "Target Field" result
			List<TargetField> targetFields = this.evaluator.getTargetFields();
 			for(TargetField targetField : targetFields){
				FieldName targetFieldName = targetField.getName();
				Object targetFieldValue = results.get(targetFieldName);
				if(targetFieldValue instanceof Computable){
				    Computable computable = (Computable)targetFieldValue;
				    Object unboxedTargetFieldValue = computable.getResult();
					String outputname = "DefaultTarget";
					if (targetFieldName!=null){
					    	outputname = targetFieldName.toString();
					}
				    String fieldUUID = getStepOption(outputname+"Selected");
					if (fieldUUID!=null){
				    	Wire<Object, String> wire = this.getWireForField(fieldUUID);
				    	if(wire!=null){
				    		
				    		if (targetField.getDataType() == DataType.DOUBLE 
				    				|| targetField.getDataType() == DataType.FLOAT)
				    		{
				    			unboxedTargetFieldValue = (double) unboxedTargetFieldValue;
				    		} else if (targetField.getDataType() == DataType.INTEGER) {
				    			unboxedTargetFieldValue = (int) unboxedTargetFieldValue;
				    		} else if (targetField.getDataType() == DataType.STRING) {
				    			unboxedTargetFieldValue = (String) unboxedTargetFieldValue;
				    		} else if (targetField.getDataType() == DataType.DATE) {
				    			unboxedTargetFieldValue = (Date) unboxedTargetFieldValue;
				    		}
				    		
				    		wire.send(unboxedTargetFieldValue);			    			
				    			
				    	}
				    }
				}
				else {
					String outputname = "DefaultTarget";
					if (targetFieldName!=null){
					    	outputname = targetFieldName.toString();
					}
				    String fieldUUID = getStepOption(outputname+"Selected");
					if (fieldUUID!=null){
				    	Wire<Object, String> wire = this.getWireForField(fieldUUID);
				    	if(wire!=null){
				    		
				    		if (targetField.getDataType() == DataType.DOUBLE 
				    				|| targetField.getDataType() == DataType.FLOAT)
				    		{
				    			targetFieldValue = (double) targetFieldValue;
				    		} else if (targetField.getDataType() == DataType.INTEGER) {
				    			targetFieldValue = (int) targetFieldValue;
				    		} else if (targetField.getDataType() == DataType.STRING) {
				    			targetFieldValue = (String) targetFieldValue;
				    		} else if (targetField.getDataType() == DataType.DATE) {
				    			targetFieldValue = (Date) targetFieldValue;
				    		}
				    		
				    		wire.send(targetFieldValue);
				    	}
				    }
				}
				List<OutputField> outputFields = this.evaluator.getOutputFields();
				for(OutputField outputField : outputFields){
					FieldName outputFieldName = outputField.getName();
				    Object outputFieldValue = results.get(outputFieldName);
				    String fieldUUID = getStepOption(outputFieldName.toString()+"Selected");
					if (fieldUUID!=null){
				    	Wire<Object, String> wire = this.getWireForField(fieldUUID);
				    	if(wire!=null){
				    		if (outputFieldValue==null || "null".equals(outputFieldValue))
					    		wire.send(null);
					    	else {
					    		if (outputField.getDataType() == DataType.DOUBLE 
					    				|| outputField.getDataType() == DataType.FLOAT)
					    		{
					    			outputFieldValue = (double) outputFieldValue;
					    		} else if (outputField.getDataType() == DataType.INTEGER) {
					    			outputFieldValue = (int) outputFieldValue;
					    		} else if (outputField.getDataType() == DataType.STRING) {
					    			outputFieldValue = (String) outputFieldValue;
					    		} else if (outputField.getDataType() == DataType.DATE) {
					    			outputFieldValue = (Date) outputFieldValue;
					    		}
					    		
					    		wire.send(outputFieldValue);
					    	}
					    					    	
				    	}
				    	
				    }
				}

			}
			return true;
		}
		
		return false;
		
	}


	private List<String> getOutputs() {
		Type t = new TypeToken<ArrayList<String>>(){}.getType();
		Gson g = new Gson();
		String opt = getStepOption("pmmlOutput");
		if(opt == null) return new ArrayList<>();
		List<String> PMMLOutputs = g.fromJson(opt, t);
		return PMMLOutputs;
	}
	public Evaluator getEvaluatorFromFile(byte[] file){
		PMML pmml = null;
		if (file!=null){
			InputStream is;
			try {	
				is = new ByteArrayInputStream(file);
				pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
			} catch (SAXException | JAXBException e) {
				return null;
			}
			ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
			Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
			evaluator.verify();
			return evaluator;
		}else{
			return null;
		}

	}
	public Evaluator getEvaluatorFromPath(String filepath){
		PMML pmml = null;
		if (filepath!=null){
			InputStream is;
			if (filepath.contains("http")) {
				try {
					is= new URL(filepath).openStream();
				} catch (IOException e) {
					return null;
				}
			    try {
					pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
				} catch (SAXException | JAXBException e) {
					return null;
				}
			}else{   	
				try {
					is = new FileInputStream(filepath);
				} catch (FileNotFoundException e) {
					return null;
				}
				try {
					pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
				} catch (SAXException | JAXBException e) {
					return null;
				}
			} 
			ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
			Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
			evaluator.verify();
			return evaluator;
		}else{
			return null;
		}

	}
	public void parseData(Map<String, Object> data) {
	}
	
	public static String getText(String englishText, String translationKey) {

	    String text = UtilString.getResourceString(translationKey);
	    if (text==null) return englishText;
	    return text;

	}

	@Override
	public ETLStepAPIVersion getAPIVersion() {
		return ETLStepAPIVersion.V1;
	}

}
