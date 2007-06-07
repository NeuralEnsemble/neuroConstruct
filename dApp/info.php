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
<script type="text/javascript" src="skin/tigris.js"></script><script src="skin/menu.js" language="javascript" type="text/javascript"></script>
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
include("ute.php"); 

$pwParam="into";
$referenceParam="reference";
$countryParam="country";

$filename="/export/home/ucgbpgl/basing/silvera";

$file=fopen($filename,"r");
$adminPasswd = trim(fgets($file));	


if (!isset($_REQUEST[$pwParam]) || $_REQUEST[$pwParam] != $adminPasswd)
{
	echo "<p>Under construction!</p>";
}
else
{
	$mainUrl = "info.php?$pwParam=$adminPasswd";

	
	
	$con = getConnection();
	
	if (!$con)
	{
	die('Could not connect: ' . mysql_error());
	}
	
	
	mysql_select_db("neuroConstruct", $con);

	$whereQualifier = "WHERE 1 ";
	$dLWhereQualifier = "WHERE 1 ";

	if (isset($_REQUEST[$referenceParam]))
	{
		$ref = scanForNasties($_REQUEST[$referenceParam]);

		$whereQualifier = $whereQualifier . " AND `reference` = $ref";
		$dLWhereQualifier = $dLWhereQualifier . " AND `reference` = $ref";

		echo "<div class='h3'><h3><i>Showing detail for request $ref!</em></i></div>";

		echo "<a href='$mainUrl'><b>Show all</b></a>";
	}

	if (isset($_REQUEST[$countryParam]))
	{
		$coun = scanForNasties($_REQUEST[$countryParam]);

		$whereQualifier = $whereQualifier . " AND `country` like '$coun' ";

		echo "<div class='h3'><h3><i>Showing detail for country $coun</em></i></div>";

		echo "<a href='$mainUrl'><b>Show all</b></a>";
	}

	$sql1 = "SELECT COUNT(*) FROM DownloadRequests ".$whereQualifier;

	echo "<div class='h3'><h3><em>Totals: </em></h3></div>";


	$totReqsQ = mysql_query($sql1);

	$totReqs = mysql_fetch_array($totReqsQ);

	$totReqsEmailQ = mysql_query("SELECT COUNT(DISTINCT email) FROM DownloadRequests ".$whereQualifier."");
	$totReqsEmail = mysql_fetch_array($totReqsEmailQ);



	echo "<table border='1' cellpadding='5'>
		<tr>
			<td>Total download requests:</td>
			<td>". $totReqs[0] ."</td>
		</tr>		
		<tr>
			<td>Total requests (distinct emails):</td>
			<td>". $totReqsEmail[0] ."</td>
		</tr>	";

	if (!isset($_REQUEST[$countryParam]))
	{

		$totDlQ = mysql_query("SELECT COUNT(*) FROM Downloads ".$dLWhereQualifier);
		$totDl = mysql_fetch_array($totDlQ);
	
		$totDlWQ = mysql_query("SELECT COUNT(*) FROM Downloads ".$dLWhereQualifier." AND `filename` like '%exe'");
		$totDlW = mysql_fetch_array($totDlWQ);
		$totDlLQ = mysql_query("SELECT COUNT(*) FROM Downloads ".$dLWhereQualifier." AND `filename` like '%sh'");
		$totDlL = mysql_fetch_array($totDlLQ);
		$totDlMQ = mysql_query("SELECT COUNT(*) FROM Downloads ".$dLWhereQualifier." AND `filename` like '%dmg'");
		$totDlM = mysql_fetch_array($totDlMQ);
	
		$totDlZQ = mysql_query("SELECT COUNT(*) FROM Downloads ".$dLWhereQualifier." AND `filename` like '%zip'");
		$totDlZ = mysql_fetch_array($totDlZQ);

		echo "<tr>
			<td>Total downloads:</td>
			<td>". $totDl[0] ."</td>
		</tr>
		<tr>
			<td>Total downloads (Windows):</td>
			<td>". $totDlW[0] ."  ...  ". round((100*$totDlW[0])/$totDl[0],2)."%</td>
		</tr>
		<tr>
			<td>Total downloads (Linux):</td>
			<td>". $totDlL[0] ." ... ". round((100*$totDlL[0])/$totDl[0],2)."%</td>
		</tr>
		<tr>
			<td>Total downloads (Mac):</td>
			<td>". $totDlM[0] ." ... ". round((100*$totDlM[0])/$totDl[0],2)."%</td>
		</tr>
		<tr>
			<td>Total downloads (Zip):</td>
			<td>". $totDlZ[0] ." ... ". round((100*$totDlZ[0])/$totDl[0],2)."%</td>
		</tr>";
	}

	echo"</table><br/>";


	if (!isset($_REQUEST[$countryParam]))
	{
		$counts = mysql_query("SELECT country, count( country ), count(DISTINCT email) counter FROM `DownloadRequests` ".
				$whereQualifier." GROUP BY country ORDER BY counter DESC");
	
		echo "<table border='2' cellpadding='3' style='border-style: solid;'>";
	
	
		$lastNum = -1;
	
		while($row = mysql_fetch_array($counts))
		{
			$country = $row['country'];
			$count = $row['counter'];
	
			$countryLink = "<a href='$mainUrl&$countryParam=$country'>$country</a>";
	
			if ($count!=$lastNum)
			{
				echo "</td></tr><tr>";
	
				echo "<td width='70' align='center'><b>$count</b></td><td width='400'>$countryLink";			
			}
			else
			{
				echo ", $countryLink";
			}
	
			$lastNum = $count;
		}
	}

	echo "</td></tr></table>";

	echo "<div class='h3'><h3><em>Requests for downloads:</em></h3></div>";
	
	
	//$result = mysql_query("SELECT * FROM DownloadRequests ORDER BY ID".$whereQualifier);
	$result = mysql_query("SELECT * FROM DownloadRequests ".$whereQualifier." ORDER BY ID DESC");

	echo "<table border='2' cellpadding='5'>
	<tr>
	<th>Who</th>  
	<th>Comments</th>
	<th>Request</th>
	<th>Downloaded</th>
	</tr>";
	
	
	while($row = mysql_fetch_array($result))
	{
		$thisRef = $row['reference'];

		$numDownloadedResult = mysql_query("SELECT COUNT(*) FROM Downloads WHERE reference='$thisRef'");
		$numDownloaded =  mysql_fetch_array($numDownloadedResult);

		echo "<tr>";

		echo "<td><b>"
			. $row['name'] . "</b><br/>" . $row['email'] . "<br/>" . $row['institution'] ."<br/>" 
			. "<b>".$row['country'] . "</b></td><td>"
			
			. "<b>Brain region:</b> ". $row['brainRegion']  . "<br/>" 
			. "<b>Research Type:</b> ". $row['researchTopic']  . "<br/>" 
			. "<b>Research:</b> ". $row['descriptionResearch']  . "<br/>" 

			. "<b>Comment:</b> ". $row['comment'] . "</td><td>"

			. date("l dS \of F Y H:i:s", $row['requestDate']) . "<br/>" 
			. "<b>Host:</b> " . $row['clientserver'] . "<br/>" 
			. "<b>Ref:</b> " . $thisRef . "</td><td>"
			. "<a href='$mainUrl&$referenceParam=$thisRef'>".$numDownloaded[0] . "</a></td>";
		echo "</tr>";
	}

	echo "</table>";

	echo "<br/>";

	if (!isset($_REQUEST[$countryParam]))
	{

		echo "<div class='h3'><h3><em>Actual downloads:</em></h3></div>";
		
		$downloadQuery = "SELECT * FROM Downloads ".$whereQualifier." ORDER BY ID";
	
		$result = mysql_query($downloadQuery);
	
		echo "<table border='1' cellpadding='3'>
		<tr>
		<th>ID</th>
		<th>Date downloaded</th>
		<th>Client server</th>
		<th>Reference</th>
		<th>File</th>
		</tr>";
	
		
		
		while($row = mysql_fetch_array($result))
		{
		echo "<tr>";
		echo "<td>" . $row['ID'] . "</td><td>"
			. date("l dS \of F Y H:i:s", $row['downloadDate']) . "</td><td>"
			. $row['clientserver'] . "</td><td>"
			. $row['reference'] . "</td><td>"
			. $row['filename'] . "</td>";
		echo "</tr>";
		}
	
		echo "</table>";
	}

	
	mysql_close($con);

}

?>



</body>
</html>