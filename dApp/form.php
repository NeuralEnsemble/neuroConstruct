<?php if (!isset($_REQUEST['dl'])) { ?>

<html>

<head>
<style type="text/css">
    
@import "../neuroConstruct/skin/tigris.css";
@import "../neuroConstruct/skin/quirks.css";
@import "../neuroConstruct/skin/inst.css";

</style>

<link media="print" href="skin/print.css" type="text/css" rel="stylesheet">
<link href="skin/forrest.css" type="text/css" rel="stylesheet">
<link rel="shortcut icon" href="images/favicon.ico">
<script type="text/javascript" src="skin/tigris.js"></script>
<script src="skin/menu.js" language="javascript" type="text/javascript"></script>

<title>neuroConstruct: Software for developing biologically realistic 3D neural networks</title>

<meta content="text/css" http-equiv="Content-style-type">

</head>


<body>

<p>
<a href="http://www.neuroConstruct.org">
	<img class="logoImage" alt="neuroConstruct" src="../neuroConstruct/images/logoMain.png">
</a></p>

<br/>

<?php 
}


include("ute.php"); 



$mailServer= "smtp-server.ucl.ac.uk";


if (isset($_REQUEST['reference']))
{
	$ref = $_REQUEST['reference'];

	nastiesPresent($ref);

 	$conn = getConnection();

    $sql="select * from DownloadRequests 
    		where reference = '$ref' ";

	$result = mysql_query($sql,$conn);

    if (!$result)
    {
        die('<p>Error: ' . mysql_error() . "</p>");
    }    
	else if (mysql_num_rows($result)== 0 )
    {
        die('<p>Error: No results returned!!' . "</p>");
    }
	else
	{
		$row = mysql_fetch_array($result);

		if (isset($_REQUEST['dl']))
		{
			$dl = $_REQUEST['dl'];
		
			nastiesPresent($dl);


			$hostname = gethostbyaddr($_SERVER['REMOTE_ADDR']);
		
			$t = time();
	
			$sql="INSERT INTO Downloads (downloadDate, clientserver, reference, filename)
					VALUES ('$t', '$hostname', '$ref', '$dl')";
		
			if (!mysql_query($sql,$conn))
			{
				die('Error in SQL: ' . mysql_error().'<br/>'."SQL: ".$sql);
			}
			else
			{
			}
				
			$filename="/export/home/ucgbpgl/basing/downloads/".$dl;

			$extension = substr($dl,strrpos($dl,".")+1);
			//header("Content-type:application/$extension");
		
			header("Content-Type: application/octet-stream");
		
			header("Content-Disposition:attachment;filename=$dl");
			header("Content-Transfer-Encoding: binary");
			header("Content-Length: ".@filesize($filename)); 
			header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
			header("Expires: 0");
	
			@readfile($filename);


			exit();

		}
		else
		{

			echo "<h2>Downloading neuroConstruct</h2>";
	/*
			echo "
				<table>
					<tr>
						<td>Name: </td>
						<td>" . $row['name'] ."</td>
					</tr>
					<tr>
						<td>Email: </td>
						<td>" . $row['email'] ."</td>
					</tr>
					<tr>
					<tr>
						<td>Institution: </td>
						<td>" . $row['institution'] ."</td>
					</tr>
					<tr>
					<tr>
						<td>Country: </td>
						<td>" . $row['country'] ."</td>
					</tr>
				</table>";*/
	
		
			//echo "<h3>Please select one of the links below to download the application.</h3>";



			echo "<p><a href ='../neuroConstruct/docs/install.html'>Installation instructions</a></p>";

			echo "<h3>The latest version contains the extra example networks in the neuroConstruct paper (Gleeson et al. Neuron 2007).</h3>";
			echo "<h3><span style='color:#FF0000;'>Uninstallation of any previous version is advised before updating.</h3></h3>";


			echo "<h4>Installer for Windows</h4>";
			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_windows_1_0_1.exe'>neuroConstruct_windows_1_0_1.exe</a></p>";
		
			echo "<h4>Installer for Linux</h4>";
			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_unix_1_0_1.sh'>neuroConstruct_unix_1_0_1.sh</a></p>";
		
			echo "<h4>Installer for Mac</h4>";
			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_macos_1_0_1.dmg'>neuroConstruct_macos_1_0_1.dmg</a></p>";
				
			echo "<h4>Zip file for manual install</h4>";
			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_1.0.1.zip'>neuroConstruct_1.0.1.zip</a></p>";
		
			echo "<h3>Older versions...</h3>";



			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_windows_0_9_8.exe'>neuroConstruct_windows_0_9_8.exe</a></p>";
		
			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_unix_0_9_8.sh'>neuroConstruct_unix_0_9_8.sh</a></p>";
		
			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_macos_0_9_8.dmg'>neuroConstruct_macos_0_9_8.dmg</a></p>";
				
			echo "<p><a href='form.php?reference=".$ref."&dl=neuroConstruct_0.9.8.zip'>neuroConstruct_0.9.8.zip</a></p>";
		
		

				
		}

		mysql_close($conn);

	}

	
}

else if (!isset($_REQUEST['email']) || $_REQUEST['email'] == "")
{

	//echo "Sendmail: " . sendmail_path;

  echo "<div class='h3'>
		<h3><em>Download neuroConstruct</em></h3>
		</div>

		<p>Please enter your details below:</p>

  <form action='form.php' method='post'>
  	<table>
  		<tr>
			<td>Name: </td>
			<td><input type='text' name='name' size='50'/> 
			</td>
			<td>*</td></td>
  		</tr>
  		<tr>
   			<td>Email: </td>
		    <td><input type='text' name='email'  size='50'/> 
			</td>
			<td>*</td></td>
  		</tr>
  		<tr>
  		<tr>
   			<td>Institution: </td>
		    <td><input type='text' name='institution' size='50' /> 
			</td>
			<td>*</td> </td>
  		</tr>
  		<tr>
  		<tr>
   			<td>Country: </td>
		    <td>
				<select name='country' >
					<option value=''>Country...</option>
					<option value='Afganistan'>Afghanistan</option>
					<option value='Albania'>Albania</option>
					<option value='Algeria'>Algeria</option>
					<option value='American Samoa'>American Samoa</option>
					<option value='Andorra'>Andorra</option>
					<option value='Angola'>Angola</option>
					<option value='Anguilla'>Anguilla</option>
					<option value='Antigua & Barbuda'>Antigua & Barbuda</option>
					<option value='Argentina'>Argentina</option>
					<option value='Armenia'>Armenia</option>
					<option value='Aruba'>Aruba</option>
					<option value='Australia'>Australia</option>
					<option value='Austria'>Austria</option>
					<option value='Azerbaijan'>Azerbaijan</option>
					<option value='Bahamas'>Bahamas</option>
					<option value='Bahrain'>Bahrain</option>
					<option value='Bangladesh'>Bangladesh</option>
					<option value='Barbados'>Barbados</option>
					<option value='Belarus'>Belarus</option>
					<option value='Belgium'>Belgium</option>
					<option value='Belize'>Belize</option>
					<option value='Benin'>Benin</option>
					<option value='Bermuda'>Bermuda</option>
					<option value='Bhutan'>Bhutan</option>
					<option value='Bolivia'>Bolivia</option>
					<option value='Bonaire'>Bonaire</option>
					<option value='Bosnia & Herzegovina'>Bosnia & Herzegovina</option>
					<option value='Botswana'>Botswana</option>
					<option value='Brazil'>Brazil</option>
					<option value='British Indian Ocean Ter'>British Indian Ocean Ter</option>
					<option value='Brunei'>Brunei</option>
					<option value='Bulgaria'>Bulgaria</option>
					<option value='Burkina Faso'>Burkina Faso</option>
					<option value='Burundi'>Burundi</option>
					<option value='Cambodia'>Cambodia</option>
					<option value='Cameroon'>Cameroon</option>
					<option value='Canada'>Canada</option>
					<option value='Canary Islands'>Canary Islands</option>
					<option value='Cape Verde'>Cape Verde</option>
					<option value='Cayman Islands'>Cayman Islands</option>
					<option value='Central African Republic'>Central African Republic</option>
					<option value='Chad'>Chad</option>
					<option value='Channel Islands'>Channel Islands</option>
					<option value='Chile'>Chile</option>
					<option value='China'>China</option>
					<option value='Christmas Island'>Christmas Island</option>
					<option value='Cocos Island'>Cocos Island</option>
					<option value='Columbia'>Columbia</option>
					<option value='Comoros'>Comoros</option>
					<option value='Congo'>Congo</option>
					<option value='Cook Islands'>Cook Islands</option>
					<option value='Costa Rica'>Costa Rica</option>
					<option value='Cote DIvoire'>Cote D'Ivoire</option>
					<option value='Croatia'>Croatia</option>
					<option value='Cuba'>Cuba</option>
					<option value='Curaco'>Curacao</option>
					<option value='Cyprus'>Cyprus</option>
					<option value='Czech Republic'>Czech Republic</option>
					<option value='Denmark'>Denmark</option>
					<option value='Djibouti'>Djibouti</option>
					<option value='Dominica'>Dominica</option>
					<option value='Dominican Republic'>Dominican Republic</option>
					<option value='East Timor'>East Timor</option>
					<option value='Ecuador'>Ecuador</option>
					<option value='Egypt'>Egypt</option>
					<option value='El Salvador'>El Salvador</option>
					<option value='Equatorial Guinea'>Equatorial Guinea</option>
					<option value='Eritrea'>Eritrea</option>
					<option value='Estonia'>Estonia</option>
					<option value='Ethiopia'>Ethiopia</option>
					<option value='Falkland Islands'>Falkland Islands</option>
					<option value='Faroe Islands'>Faroe Islands</option>
					<option value='Fiji'>Fiji</option>
					<option value='Finland'>Finland</option>
					<option value='France'>France</option>
					<option value='French Guiana'>French Guiana</option>
					<option value='French Polynesia'>French Polynesia</option>
					<option value='French Southern Ter'>French Southern Ter</option>
					<option value='Gabon'>Gabon</option>
					<option value='Gambia'>Gambia</option>
					<option value='Georgia'>Georgia</option>
					<option value='Germany'>Germany</option>
					<option value='Ghana'>Ghana</option>
					<option value='Gibraltar'>Gibraltar</option>
					<option value='Great Britain'>Great Britain</option>
					<option value='Greece'>Greece</option>
					<option value='Greenland'>Greenland</option>
					<option value='Grenada'>Grenada</option>
					<option value='Guadeloupe'>Guadeloupe</option>
					<option value='Guam'>Guam</option>
					<option value='Guatemala'>Guatemala</option>
					<option value='Guinea'>Guinea</option>
					<option value='Guyana'>Guyana</option>
					<option value='Haiti'>Haiti</option>
					<option value='Hawaii'>Hawaii</option>
					<option value='Honduras'>Honduras</option>
					<option value='Hong Kong'>Hong Kong</option>
					<option value='Hungary'>Hungary</option>
					<option value='Iceland'>Iceland</option>
					<option value='India'>India</option>
					<option value='Indonesia'>Indonesia</option>
					<option value='Iran'>Iran</option>
					<option value='Iraq'>Iraq</option>
					<option value='Ireland'>Ireland</option>
					<option value='Isle of Man'>Isle of Man</option>
					<option value='Israel'>Israel</option>
					<option value='Italy'>Italy</option>
					<option value='Jamaica'>Jamaica</option>
					<option value='Japan'>Japan</option>
					<option value='Jordan'>Jordan</option>
					<option value='Kazakhstan'>Kazakhstan</option>
					<option value='Kenya'>Kenya</option>
					<option value='Kiribati'>Kiribati</option>
					<option value='Korea North'>Korea North</option>
					<option value='Korea Sout'>Korea South</option>
					<option value='Kuwait'>Kuwait</option>
					<option value='Kyrgyzstan'>Kyrgyzstan</option>
					<option value='Laos'>Laos</option>
					<option value='Latvia'>Latvia</option>
					<option value='Lebanon'>Lebanon</option>
					<option value='Lesotho'>Lesotho</option>
					<option value='Liberia'>Liberia</option>
					<option value='Libya'>Libya</option>
					<option value='Liechtenstein'>Liechtenstein</option>
					<option value='Lithuania'>Lithuania</option>
					<option value='Luxembourg'>Luxembourg</option>
					<option value='Macau'>Macau</option>
					<option value='Macedonia'>Macedonia</option>
					<option value='Madagascar'>Madagascar</option>
					<option value='Malaysia'>Malaysia</option>
					<option value='Malawi'>Malawi</option>
					<option value='Maldives'>Maldives</option>
					<option value='Mali'>Mali</option>
					<option value='Malta'>Malta</option>
					<option value='Marshall Islands'>Marshall Islands</option>
					<option value='Martinique'>Martinique</option>
					<option value='Mauritania'>Mauritania</option>
					<option value='Mauritius'>Mauritius</option>
					<option value='Mayotte'>Mayotte</option>
					<option value='Mexico'>Mexico</option>
					<option value='Midway Islands'>Midway Islands</option>
					<option value='Moldova'>Moldova</option>
					<option value='Monaco'>Monaco</option>
					<option value='Mongolia'>Mongolia</option>
					<option value='Montserrat'>Montserrat</option>
					<option value='Morocco'>Morocco</option>
					<option value='Mozambique'>Mozambique</option>
					<option value='Myanmar'>Myanmar</option>
					<option value='Nambia'>Nambia</option>
					<option value='Nauru'>Nauru</option>
					<option value='Nepal'>Nepal</option>
					<option value='Netherland Antilles'>Netherland Antilles</option>
					<option value='Netherlands'>Netherlands</option>
					<option value='Nevis'>Nevis</option>
					<option value='New Caledonia'>New Caledonia</option>
					<option value='New Zealand'>New Zealand</option>
					<option value='Nicaragua'>Nicaragua</option>
					<option value='Niger'>Niger</option>
					<option value='Nigeria'>Nigeria</option>
					<option value='Niue'>Niue</option>
					<option value='Norfolk Island'>Norfolk Island</option>
					<option value='Norway'>Norway</option>
					<option value='Oman'>Oman</option>
					<option value='Pakistan'>Pakistan</option>
					<option value='Palau Island'>Palau Island</option>
					<option value='Palestine'>Palestine</option>
					<option value='Panama'>Panama</option>
					<option value='Papua New Guinea'>Papua New Guinea</option>
					<option value='Paraguay'>Paraguay</option>
					<option value='Peru'>Peru</option>
					<option value='Phillipines'>Philippines</option>
					<option value='Pitcairn Island'>Pitcairn Island</option>
					<option value='Poland'>Poland</option>
					<option value='Portugal'>Portugal</option>
					<option value='Puerto Rico'>Puerto Rico</option>
					<option value='Qatar'>Qatar</option>
					<option value='Reunion'>Reunion</option>
					<option value='Romania'>Romania</option>
					<option value='Russia'>Russia</option>
					<option value='Rwanda'>Rwanda</option>
					<option value='St Barthelemy'>St Barthelemy</option>
					<option value='St Eustatius'>St Eustatius</option>
					<option value='St Helena'>St Helena</option>
					<option value='St Kitts-Nevis'>St Kitts-Nevis</option>
					<option value='St Lucia'>St Lucia</option>
					<option value='St Maarten'>St Maarten</option>
					<option value='St Pierre & Miquelon'>St Pierre & Miquelon</option>
					<option value='St Vincent & Grenadines'>St Vincent & Grenadines</option>
					<option value='Saipan'>Saipan</option>
					<option value='Samoa'>Samoa</option>
					<option value='Samoa American'>Samoa American</option>
					<option value='San Marino'>San Marino</option>
					<option value='Sao Tome & Principe'>Sao Tome & Principe</option>
					<option value='Saudi Arabia'>Saudi Arabia</option>
					<option value='Senegal'>Senegal</option>
					<option value='Seychelles'>Seychelles</option>
					<option value='Serbia & Montenegro'>Serbia & Montenegro</option>
					<option value='Sierra Leone'>Sierra Leone</option>
					<option value='Singapore'>Singapore</option>
					<option value='Slovakia'>Slovakia</option>
					<option value='Slovenia'>Slovenia</option>
					<option value='Solomon Islands'>Solomon Islands</option>
					<option value='Somalia'>Somalia</option>
					<option value='South Africa'>South Africa</option>
					<option value='Spain'>Spain</option>
					<option value='Sri Lanka'>Sri Lanka</option>
					<option value='Sudan'>Sudan</option>
					<option value='Suriname'>Suriname</option>
					<option value='Swaziland'>Swaziland</option>
					<option value='Sweden'>Sweden</option>
					<option value='Switzerland'>Switzerland</option>
					<option value='Syria'>Syria</option>
					<option value='Tahiti'>Tahiti</option>
					<option value='Taiwan'>Taiwan</option>
					<option value='Tajikistan'>Tajikistan</option>
					<option value='Tanzania'>Tanzania</option>
					<option value='Thailand'>Thailand</option>
					<option value='Togo'>Togo</option>
					<option value='Tokelau'>Tokelau</option>
					<option value='Tonga'>Tonga</option>
					<option value='Trinidad & Tobago'>Trinidad & Tobago</option>
					<option value='Tunisia'>Tunisia</option>
					<option value='Turkey'>Turkey</option>
					<option value='Turkmenistan'>Turkmenistan</option>
					<option value='Turks & Caicos Is'>Turks & Caicos Is</option>
					<option value='Tuvalu'>Tuvalu</option>
					<option value='Uganda'>Uganda</option>
					<option value='Ukraine'>Ukraine</option>
					<option value='United Arab Erimates'>United Arab Emirates</option>
					<option value='United Kingdom'>United Kingdom</option>
					<option value='United States of America'>United States of America</option>
					<option value='Uraguay'>Uruguay</option>
					<option value='Uzbekistan'>Uzbekistan</option>
					<option value='Vanuatu'>Vanuatu</option>
					<option value='Vatican City State'>Vatican City State</option>
					<option value='Venezuela'>Venezuela</option>
					<option value='Vietnam'>Vietnam</option>
					<option value='Virgin Islands (Brit)'>Virgin Islands (Brit)</option>
					<option value='Virgin Islands (USA)'>Virgin Islands (USA)</option>
					<option value='Wake Island'>Wake Island</option>
					<option value='Wallis & Futana Is'>Wallis & Futana Is</option>
					<option value='Yemen'>Yemen</option>
					<option value='Zaire'>Zaire</option>
					<option value='Zambia'>Zambia</option>
					<option value='Zimbabwe'>Zimbabwe</option>
				</select> 
			</td>
			<td>*
			</td>
  		</tr>

  		<tr>
   			<td>Neural system of interest:  </td>
		    <td><select name='brainRegionSel'>


					<option value='Unspecified'>Please select...</option>
					<option value='Generic neural circuits'>Generic neural circuits</option>
					<option value='Cortex'>Cortex</option>
					<option value='Thalamus'>Thalamus</option>
					<option value='Auditory system'>Auditory system</option>
					<option value='Olfactory system'>Olfactory system</option>
					<option value='Hippocampus'>Hippocampus</option>
					<option value='Cerebellum'>Cerebellum</option>
					<option value='Basal ganglia'>Basal ganglia</option>
					<option value='Hypothalamus'>Hypothalamus</option>
					<option value='Visual system'>Visual system</option>
					<option value='Invertebrate systems'>Invertebrate systems</option>
					<option value='Brain stem'>Brain stem</option>
					<option value='Spinal cord'>Spinal cord</option>


				</select>  or: <input type='text' name='brainRegion' size='22' /> 
			</td>
			<td>*</td>
  		</tr>

  		<tr>
   			<td>Research focus: </td>
		    <td>
				<select name='researchTopic'>
					<option value='Unspecified'>Please select...</option>
					<option value='Basic research'>Basic research</option>
					<option value='Clinical research'>Clinical research</option>
					<option value='Commercially focused research'>Commercially focused research</option>
				</select> 
			</td>
			<td>*</td>
  		</tr>

  		<tr>
   			<td>Research aims: </td>
			<td><textarea name ='descriptionResearch' rows='5' cols='42'></textarea></td>
  		</tr>

  		<tr>
   			<td>Any other comment: </td>
			<td><textarea name ='comment' rows='5' cols='42'></textarea></td>
  		</tr>


  		<tr>
    		<td><input type='submit' value='Submit'/></td>
      	</tr>
    


		<tr>
    		<td colspan='2'>* = required field</td>
      	</tr>
    
	
		<tr>
			<td colspan='3' width='500' >We ask for an email address and other details as numbers of downloads, geographical spread of users etc. is very interesting
			to funding bodies, and will help ensure that neuroConstruct stays in development and remains free to the community.
			 We won't give names or email addresses out to any 3rd party.
			</td>
		</tr>
    

 </table>
    </form>";

}



else
{
  $mailcheck = emailOk($_REQUEST['email']);

    $name = scanForNasties($_POST["name"]);
    $comment = scanForNasties($_POST["comment"]);
    $institution = scanForNasties($_POST["institution"]);
    $country = scanForNasties($_POST["country"]);
    $brainRegion = scanForNasties($_POST["brainRegion"]);
    $brainRegionSel = scanForNasties($_POST["brainRegionSel"]);
    $researchTopic = scanForNasties($_POST["researchTopic"]);
    $descriptionResearch = scanForNasties($_POST["descriptionResearch"]);

  if ($mailcheck==FALSE)
  {
    echo "Invalid email address!";
    echo "<br/><br/><a href='javascript: history.go(-1)'>Back</a>";
  }
  else if ($name=='')
  {
    echo "Invalid name.";
    echo "<br/><br/><a href='javascript: history.go(-1)'>Back</a>";
  }
  else if ($institution=='')
  {
    echo "Invalid Institution.";
    echo "<br/><br/><a href='javascript: history.go(-1)'>Back</a>";
  }
  else if ($country=='' || $country=='Country...')
  {
    echo "Invalid country.";
    echo "<br/><br/><a href='javascript: history.go(-1)'>Back</a>";
  }
  else if ($brainRegionSel=='Unspecified'  && $brainRegion=='')
  {
    echo "Invalid brain region.";
    echo "<br/><br/><a href='javascript: history.go(-1)'>Back</a>";
  }
  else if ($researchTopic=='' || $researchTopic=='Unspecified')
  {
    echo "Invalid research focus.";
    echo "<br/><br/><a href='javascript: history.go(-1)'>Back</a>";
  }
  else
  {
    $email = $_POST["email"];

    $hostname = gethostbyaddr($_SERVER['REMOTE_ADDR']);

	$randNum = rand();

    $to = "p.gleeson@ucl.ac.uk";

    $subject = "The code has been downloaded by: $email";

	$brainRegionToTake = $brainRegionSel;

	if ($brainRegion!='')
	{
		$brainRegionToTake = $brainRegion;
	}

    $message =  $subject . "\r\n\r\n" .
             	"Name: $name " . "\r\n".
				"Email: $email " . "\r\n".
				"Institution: $institution " . "\r\n".
				"Country: $country " . "\r\n".

			   	"Brain region:  $brainRegionToTake " . "\r\n".
				"Research Type: $researchTopic "  . "\r\n".
			 	"Research description: $descriptionResearch "  . "\r\n".


				"Comment: $comment" . "\r\n\r\n";

    $from = "info@neuroconstruct.org";
    $headers = "From: $from " . "\r\n" . "Reply-To: $from";

	//$extraArgs = "-f$from";	

		$extraArgs = "-f$from -pSMTP:smtp-server.ucl.ac.uk";



    mail($to,$subject,$message,$headers, $extraArgs);


    //echo "A confirmation Mail has been sent to $to about $email";


 	$conn = getConnection();



    $t = time();


    $sql="INSERT INTO DownloadRequests (name, email, comment, country, institution, 
               requestDate, clientserver, reference, brainRegion, researchTopic, descriptionResearch)
    		VALUES ('$name','$email','$comment','$country','$institution', 
				'$t', '$hostname', '$randNum', '$brainRegionToTake', '$researchTopic', '$descriptionResearch')";

    if (!mysql_query($sql,$conn))
    {
        die('Error: ' . mysql_error());
    }
	else
	{

		$to = $email;
	
		$subject = "Downloading neuroConstruct";

		$myURL = "http://www.physiol.ucl.ac.uk/research/silver_a/nCinfo/form.php?reference=$randNum";
	
		$message =  "Hi," . "\r\n\r\n" .
					"Thank you for your interest in neuroConstruct. " . "\r\n".
				     "The latest version of the software can be downloaded from: "  . "\r\n\r\n".
				     "$myURL "  . "\r\n\r\n".
				     "Regards, "  . "\r\n".
				     "The neuroConstruct team "  . "\r\n";
	
		$from = "info@neuroConstruct.org";

		$headers = "From: $from " . "\r\n" . "Reply-To: $from";

		$extraArgs = "-f$from -pSMTP:smtp-server.ucl.ac.uk";
	
		//$accepted = mail($to,$subject,$message,$headers, $extraArgs);
	
	
		//echo "<p>A mail has been sent to $to with a link for downloading the application</p>";
	

		//echo "<p>Note: if you do not receive the mail within the next hour please contact <a href='mailto:info@neuroConstruct.org'>info@neuroConstruct.org</a></p>";


		echo "<p>Thank you for your interest in neuroConstruct</p>";

		echo "<p>Please <a href='$myURL'>click here</a> to continue</p>";
	}

    mysql_close($conn);

  }
}
?>
<?php if (!isset($_REQUEST['dl'])) { ?>
<br/><br/>
<p><i>If there are any problems with this download procedure please contact <b>info - at - neuroConstruct.org</b></i></p>

</body>
</html>

<?php }?>
