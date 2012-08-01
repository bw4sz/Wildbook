<%@ page contentType="text/html; charset=utf-8" language="java" import="org.ecocean.*, java.util.*,javax.jdo.*,java.io.File" %>

<%--
  ~ The Shepherd Project - A Mark-Recapture Framework
  ~ Copyright (C) 2012 Jason Holmberg
  ~
  ~ This program is free software; you can redistribute it and/or
  ~ modify it under the terms of the GNU General Public License
  ~ as published by the Free Software Foundation; either version 2
  ~ of the License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  --%>

<%
Shepherd myShepherd = new Shepherd();
try {

	//get the encounter number
	String encNum = request.getParameter("encounterNumber");
	
	//set up the JDO pieces and Shepherd

	myShepherd.beginDBTransaction();
	Encounter enc=myShepherd.getEncounter(encNum);
	boolean hasPhotos=false;
	if((enc.getSinglePhotoVideo()!=null)&&(enc.getSinglePhotoVideo().size()>0)){hasPhotos=true;}

	//let's set up references to our file system components
	String rootWebappPath = getServletContext().getRealPath("/");
	File webappsDir = new File(rootWebappPath).getParentFile();
	File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName());
	File encountersDir=new File(shepherdDataDir.getAbsolutePath()+"/encounters");
	File thisEncounterDir = new File(encountersDir, encNum);

	%>
	<p><strong>Matching Algorithm</strong></p>

<%
	//test comment
	if(hasPhotos){
		
		File processedImage=new File("/foo/bar");
		File enhancedImage=new File("/foo/bar");
		File FTFile=new File("/foo/bar");
	
		boolean hasProcessedImage=false;
		String matchingImageName="";
		List<SinglePhotoVideo> myphots=enc.getSinglePhotoVideo();
		int myPhotsSize=myphots.size();
		for(int t=0;t<myPhotsSize;t++){
			SinglePhotoVideo spv=myphots.get(t);
			String spvName=spv.getFilename().replaceAll(".jpg", "_CR.jpg").replaceAll(".JPG","_CR.JPG");
			File matchOutput=new File(thisEncounterDir, (spv.getDataCollectionEventID()+"_matchOutput.xhtml"));
			File spvCRFile=new File(thisEncounterDir,spvName);
			if(spvCRFile.exists()){
				hasProcessedImage=true;
				matchingImageName=spvCRFile.getName();
				processedImage=spvCRFile;
				enhancedImage=new File(thisEncounterDir,spvCRFile.getName().replaceAll("_CR", "_EH"));
				FTFile=new File(thisEncounterDir,spvCRFile.getName().replaceAll("_CR", "_FT"));
				if(FTFile.exists() && ((request.isUserInRole("admin"))||(request.isUserInRole("imageProcessor")))){
					
				%>
				
			
					<p style="background-color:#f0f0f0;"><em>Extracted Feature Image for Image <%=(t+1) %>.</em></p>
						<p><img width="300px" height="*" src="/<%=shepherdDataDir.getName() %>/encounters/<%=encNum %>/<%=FTFile.getName()%>"/></p>
							<p><em>Remove the processed, cropped manta patterning image.</em></p>
		               		<p>
		               			<form action="../EncounterAddMantaPattern" method="post" name="EncounterAddMantaPattern">
		               				<input name="action" type="hidden" value="imageremove" id="action" />
		            				<input name="number" type="hidden" value="<%=encNum%>" id="number" />
									<input name="dataCollectionEventID" type="hidden" value="<%=spv.getDataCollectionEventID() %>" id="dataCollectionEventID" />
									
		            				<p><input name="addtlFile" type="submit" id="addtlFile" value="Remove the file" /></p>
		        				</form>
		        		 	</p>
					<%
					//if(){
						if(!matchOutput.exists()){
						%>
							<p>No match results file was found.</p>
								<p><em>Scan the manta patterning image.</em></p>
		               		<p>
		               			<form action="../EncounterAddMantaPattern" method="post" name="EncounterAddMantaPattern">
		               				<input name="action" type="hidden" value="rescan" id="action" />
		            				<input name="number" type="hidden" value="<%=encNum%>" id="number" />
									<input name="dataCollectionEventID" type="hidden" value="<%=spv.getDataCollectionEventID() %>" id="dataCollectionEventID" />
									
		            				<p><input name="addtlFile" type="submit" id="addtlFile" value="Scan" /></p>
		        				</form>
		        		 	</p> 
						<%
						}
						else{
						%>
							<p><em>Inspect the algorithm results</em></p>
							<p>A match results file was found: <a href="/<%=shepherdDataDir.getName() %>/encounters/<%=encNum %>/<%=matchOutput.getName()%>">Click here.</a></p>
							
							<p><em>Rescan the manta patterning image.</em></p>
		               		<p>
		               			<form action="../EncounterAddMantaPattern" method="post" name="EncounterAddMantaPattern">
		               				<input name="action" type="hidden" value="rescan" id="action" />
		            				<input name="number" type="hidden" value="<%=encNum%>" id="number" />
									<input name="dataCollectionEventID" type="hidden" value="<%=spv.getDataCollectionEventID() %>" id="dataCollectionEventID" />
									
		            				<p><input name="addtlFile" type="submit" id="addtlFile" value="Rescan" /></p>
		        				</form>
		        		 	</p>   

		    <%
						
						}
					//}
				}//if ftfile exists
				} 
						


			}

			
		
	
	

if((request.isUserInRole("admin"))||(request.isUserInRole("imageProcessor"))){
%>

<br />
<p style="background-color:#f0f0f0;"><em>Upload or replace a processed, cropped manta patterning image.</em></p>
      <p><form action="../EncounterAddMantaPattern" method="post"
            enctype="multipart/form-data" name="EncounterAddMantaPattern"><input
        name="action" type="hidden" value="imageadd" id="action" />
        <input name="number" type="hidden" value="<%=encNum%>" id="number" />
        
        <%
        
        //we now need to figure out which photo is the source of the uploaded image
        //TBD
        if(enc.getSinglePhotoVideo().size()==1){
        	//here we have only one photo and don't need to ask the user
        	%>
        	<input name="photoNumber" type="hidden" value="<%=enc.getImages().get(0).getDataCollectionEventID()%>" id="photoNumber" />
        	<%
        }

        else{
        	//in this case, we need to ask the user to tell us which photo is the source image
        %>
        
        <p>Image to upload processed image for: <select name="photoNumber">
          <%
            for (int rmi = 0; rmi < enc.getSinglePhotoVideo().size(); rmi++) {
          %>
          <option value="<%=enc.getImages().get(rmi).getDataCollectionEventID()%>"><%=(rmi+1)%></option>
          <%
            }
          %>
        </select><p/>
        
        <%	
        }
        %>
        <p><strong><img align="absmiddle" src="../images/upload_small.gif"/> Select file:</strong>
        <input name="file2add" type="file" size="20" /></p>
        <p><input name="addtlFile" type="submit" id="addtlFile"
                  value="Upload" /></p>
     </form></p>
     <%
	}
} 
else{
	%>
	<p>No photos found for encounter.</p>
	<%
}
myShepherd.rollbackDBTransaction();
myShepherd.closeDBTransaction();
}

catch(Exception e){
	e.printStackTrace();
	myShepherd.rollbackDBTransaction();
	myShepherd.closeDBTransaction();
}
%>


