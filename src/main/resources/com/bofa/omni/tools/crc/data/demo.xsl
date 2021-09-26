<?xml version="1.0" encoding="utf-8" ?>
<fo:root
		xml:lang="en-US"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
		xmlns:gsp='http://groovy.codehaus.org/2005/gsp'
		xmlns:svg='http://www.w3.org/2000/svg'
		font-family="any" font-style="normal" font-weight="normal">
	<fo:layout-master-set>
		<fo:simple-page-master page-width="11in"
							   page-height="8.5in" master-name="all">
			<fo:region-body margin="0px 0px 0px 0px" />
			<fo:region-before region-name="page-header" extent="1.27in" background-color="#ffdeb3" />
			<fo:region-after region-name="page-footer" extent="0.5in" background-color="#ffdeb3" />
		</fo:simple-page-master>
		<fo:page-sequence-master master-name="all-pages">
			<fo:repeatable-page-master-alternatives>
				<fo:conditional-page-master-reference
						page-position="any" master-reference="all" />
			</fo:repeatable-page-master-alternatives>
		</fo:page-sequence-master>
	</fo:layout-master-set>
	<fo:page-sequence master-reference="all-pages">
		<fo:static-content flow-name="page-header">
			<fo:block>
			</fo:block>
		</fo:static-content>
		<fo:static-content flow-name="page-footer">
			<fo:block>
			</fo:block>
		</fo:static-content>
		<fo:flow flow-name="xsl-region-body">

		</fo:flow>
	</fo:page-sequence>
</fo:root>
