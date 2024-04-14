package org.example.employee_map;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class EmployeeDataMapXmlAdapter extends XmlAdapter<DataMap, Map<String,Object>>
{
	// Represents the logger for this class.
	private static Logger logger = LoggerFactory.getLogger(EmployeeDataMapXmlAdapter.class);
	public static Logger getLogger() { return logger; }

	@Override
	public Map<String,Object> unmarshal(DataMap dataMap) throws Exception
	{
		getLogger().trace("unmarshal: {}", dataMap);
		Map<String, Object> map = new LinkedHashMap<>();
		for ( Entry entry : dataMap.getEntry() )
			map.put(entry.getKey(), entry.getValue());
		return map;
	}

	@Override
	public DataMap marshal(Map<String,Object> map) throws Exception
	{
		getLogger().trace("marshal: {}", map);
		DataMap dataMap = new DataMap();
		if ( map != null )
		{
			for ( java.util.Map.Entry<String, Object> entry : map.entrySet() )
			{
				Entry data = new Entry();
				data.setKey(entry.getKey());
				data.setValue(entry.getValue());
				dataMap.getEntry().add(data);
			}
		}
		return dataMap;
	}
}
