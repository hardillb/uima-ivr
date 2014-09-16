package uk.me.hardill.uima.web;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.pear.tools.PackageBrowser;
import org.apache.uima.pear.tools.PackageInstaller;
import org.apache.uima.pear.tools.PackageInstallerException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.CasPool;
import org.apache.uima.util.XMLInputSource;
import org.json.JSONException;
import org.json.JSONObject;

public class Processor {

	private static CasPool casPool;
	private static AnalysisEngine ae;

	public Processor(String path) {

		File installDir = new File(path,"installedPears");
		if (!installDir.exists()) {
			installDir.mkdir();
		}
		File pearFile = new File(path,"test.pear");
		boolean doVerification = true;

		try {
			// install PEAR package
			PackageBrowser instPear = PackageInstaller.installPackage(
					installDir, pearFile, doVerification);

			// retrieve installed PEAR data
			// PEAR package classpath
			String classpath = instPear.buildComponentClassPath();
			// PEAR package datapath
			String datapath = instPear.getComponentDataPath();
			// PEAR package main component descriptor
			String mainComponentDescriptor = instPear
					.getInstallationDescriptor().getMainComponentDesc();
			// PEAR package component ID
			String mainComponentID = instPear.getInstallationDescriptor()
					.getMainComponentId();
			// PEAR package pear descriptor
			String pearDescPath = instPear.getComponentPearDescPath();

			// Create a default resouce manager
			ResourceManager rsrcMgr = UIMAFramework.newDefaultResourceManager();

			// Create analysis engine from the installed PEAR package using
			// the created PEAR specifier
			XMLInputSource in = new XMLInputSource(
					instPear.getComponentPearDescPath());
			ResourceSpecifier specifier = UIMAFramework.getXMLParser()
					.parseResourceSpecifier(in);
			ae = UIMAFramework.produceAnalysisEngine(specifier,
					rsrcMgr, null);

			casPool = new CasPool(5, ae);

		} catch (PackageInstallerException ex) {
			// catch PackageInstallerException - PEAR installation failed
			ex.printStackTrace();
			System.out.println("PEAR installation failed");
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Error retrieving installed PEAR settings");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public JSONObject process(String text) {
		JSONObject json = new JSONObject();
		try {
			// Create a CAS with a sample document text
			CAS cas = casPool.getCas(15);
			cas.setDocumentText(text);
			cas.setDocumentLanguage("en");
			
			

			// Process the sample document
			ae.process(cas);

			FSIterator<Annotation> annotationsIterator = cas.getJCas()
					.getAnnotationIndex().iterator();
			while (annotationsIterator.hasNext()) {
				Annotation annotation = annotationsIterator.next();
				//System.out.println(annotation.getType().getName());
				//System.out.println(annotation.toString());
				if (annotation.getType().getName().equals("uk.me.hardill.pbx.Actions") ) {
					System.out.println("action: " +annotation.getCoveredText());
					try {
						json.put("action", annotation.getCoveredText());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (annotation.getType().getName().equals("uk.me.hardill.pbx.TVChannels")) {
					//System.out.println("-" + annotation.getCoveredText());
					//System.out.println(annotation.toString());
					Vector<Feature> features =  annotation.getType().getAppropriateFeatures();
					Iterator<Feature> fIterator = features.iterator();
					while (fIterator.hasNext()) {
						Feature f = fIterator.next();
						if (f.getShortName().equals("lemma")){
							Vector<Feature> lemmaFeatures = annotation.getFeatureValue(f).getType().getAppropriateFeatures();
							Iterator<Feature> lfIterator = lemmaFeatures.iterator();
							while (lfIterator.hasNext()) {
								Feature lf = lfIterator.next();
								if (lf.getShortName().equals("key")) {
									System.out.println("channel: " + annotation.getFeatureValue(f).getStringValue(lf));
									try {
										json.put("channel", annotation.getFeatureValue(f).getStringValue(lf));
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
				} else if (annotation.getType().getName().equals("uk.me.hardill.pbx.en.Time")) {
					String hour = null;
					String min = null;
					String am = null;
					//System.out.println(annotation.getCoveredText());
					Vector<Feature> features =  annotation.getType().getAppropriateFeatures();
					Iterator<Feature> fIterator = features.iterator();
					while (fIterator.hasNext()) {
						Feature f = fIterator.next();
						if (f.getShortName().equals("hour")){
							System.out.println("hour: " + annotation.getStringValue(f));
							hour = annotation.getStringValue(f);
						} else if (f.getShortName().equals("minutes")){
							System.out.println("minutes: " + annotation.getStringValue(f));
							min = annotation.getStringValue(f);
						} else if (f.getShortName().equals("am")){
							System.out.println("am: " + annotation.getStringValue(f));
							am = annotation.getStringValue(f);
						}
					}
					
					String time = hour + ":" + (min.equals("") ? "00": min) + " " +am;
					try {
						json.put("time", time);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (CASRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AnalysisEngineProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

}
