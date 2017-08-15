package com.project.gossip.Helpers;

import com.project.gossip.constants.Helpers;
import org.junit.Assert;
import org.junit.Test;
public class HelpersTest {

	@Test
	public void ipAddressConversionTest() {
		String ip = "111.111.111.111";
		String result = "";
		try{
			result = com.project.gossip.constants.Helpers.convertIntIpToString(com.project.gossip.constants.Helpers.convertIpStringToInt(ip));
		}
		catch (Exception exp){
			exp.printStackTrace();
		}
		Assert.assertEquals(ip, result);
	}
}
