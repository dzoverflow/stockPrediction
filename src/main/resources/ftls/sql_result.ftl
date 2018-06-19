
<#if data.queryDataList?? && (data.queryDataList?size > 0)>
<#list data.queryDataList as data_obj>

<tr style='height: 23.45pt'>
	<td width=113 colspan=2 style='width:84.65pt;border:solid #7BA0CD 1.0pt;background:#F2F2F2;padding:0cm 5.4pt 0cm 5.4pt;height:24.2pt'>
		<p class=MsoNormal>
			<b><span style='font-family: 宋体'>
			queryTitle = ${data_obj.queryDesc.queryTitle!}<br>
			queryDb = ${data_obj.queryDesc.queryDb!}<br>
			querySql = ${data_obj.queryDesc.querySql!}<br>
			</span></b>
		</p>
	</td>
</tr>

<tr style='height: 23.45pt'>
	<td width=113 colspan=2 style='width:84.65pt;border:solid #7BA0CD 1.0pt;background:#F2F2F2;padding:0cm 5.4pt 0cm 5.4pt;height:24.2pt'>
		<p class=MsoNormal>
			<b><span style='font-family: 宋体'>
			
				<p><br>
				<table class=MsoNormalTable border=1 cellspacing=0 cellpadding=0 width=850 style='border-collapse:collapse'>
					<#if data_obj?? && (data_obj.dataList?size > 0)>
					
						<#list data_obj.column as column>
							<th style='border:solid #7BA0CD 1.0pt;background:#C9C6B8;padding:0cm 5.4pt 0cm 5.4pt;height:20pt' nowrap="nowrap">
								<p class=MsoNormal>
								  	<b><span style='font-family: 宋体'>
										${column!}
									</span></b>
								</p>
							</th>
						</#list>

						<#list data_obj.dataList as objs>
							<tr style='height: 22.7pt'>
							
								<#list objs as obj>
									<td style='border:solid #7BA0CD 1.0pt;background:#F2F2F2;padding:0cm 5.4pt 0cm 5.4pt;height:20pt' nowrap="nowrap">
										<p class=MsoNormal>
										  	<b><span style='font-family: 宋体'>
												${obj!}
											</span></b>
										</p>
									</td>
								</#list>
							</tr>
						</#list>
					</#if>
				</table>
				</p>
			
			</span></b>
		</p>
	</td>
</tr>

</#list>
</#if>
