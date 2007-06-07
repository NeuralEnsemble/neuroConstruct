<?php 

function nastiesPresent($field)
{
  	if(eregi(":",$field) || eregi("/",$field))
    {
		// Not giving them too many details...
			die('Illegal character in string: ' . $field);
    }
  	else
    {
    	return FALSE;
    }
}

function scanForNasties($field)
{
	nastiesPresent($field);
    return $field;

}

function emailOk($field)
{
  	if(!nastiesPresent($field) && eregi('@', $field) && eregi('.', $field))
    {
    	return TRUE;
    }
  	else
    {
    	return FALSE;
    }
}

function getConnection()
{
	$dbServer= "localhost";
	$dbFilename="/export/home/ucgbpgl/basing/neuroC";
	$dbUser="padraig";

	$passwd = "";

	if(is_file($dbFilename))
	{
		//echo ("<p>$filename is a is_file</p>");
	
		$file=fopen($dbFilename,"r");
	
		$passwd = trim(fgets($file));
	
		//echo ("<p>---$passwd---</p>");
	
		fclose($file);

		$conn = mysql_connect($dbServer,$dbUser,$passwd);

		if (!$conn)
		{
			die('Could not connect: ' . mysql_error());
			return null;
		}
	
	
		mysql_select_db("neuroConstruct", $conn);

		return $conn;
	
	}
	else
	{
		echo ("$dbFilename is not a is_file");
		return null;
	}

}

?>
