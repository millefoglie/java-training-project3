<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
							  xmlns:fin="www.example.org/xmlns/financial"
		   					  xmlns:bank="www.example.org/xmlns/bank">
<xsl:output
    method="html"
    omit-xml-declaration="yes"
    encoding="UTF-8"
    indent="yes" />

<xsl:template match="/">
	<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
	<html>
	<head>
		<title>Deposits</title>
		<link rel="stylesheet" href="css/style.css"/>
	</head>
	<body>
		<h1>Deposits</h1>
		<table>
			<tr>
				<th>id</th>
				<th>Depositor</th>
				<th>Account Id</th>
				<th>Type</th>
				<th>Bank</th>
				<th>Country</th>
				<th>Amount on Deposit</th>
				<th>Interest</th>
				<th>Time Constraint</th>
			</tr>
			<xsl:for-each select="deposits/deposit">
				<tr>
					<td class="col-id"><xsl:value-of select="@id"/></td>
					<td class="col-depositor"><xsl:value-of select="depositor"/></td>
					<td class="col-account-id"><xsl:value-of select="account-id"/></td>
					<td class="col-type"><xsl:value-of select="@type"/></td>
					<td class="col-bank"><xsl:value-of select="@bank:name"/></td>
					<td class="col-country"><xsl:value-of select="@country"/></td>
					<td class="col-amount"><xsl:value-of select="format-number(fin:amount-on-deposit, '##,###.00')"/></td>
					<td class="col-interest"><xsl:value-of select="format-number(fin:interest, '###')"/>%</td>
					<td class="col-time"><xsl:value-of select="time-constraint"/></td>
				</tr>
			</xsl:for-each>
		</table>
	</body>
	</html>	
	<xsl:message>XSL processing finished</xsl:message>
</xsl:template>

</xsl:stylesheet>
