package project.client;

import project.client.rpc.RpcInterface;
import project.client.rpc.RpcInterfaceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ServletRpc {

	private static RpcInterfaceAsync async = null;

	public static void initAsyncInterface (String path) {
		async = (RpcInterfaceAsync) GWT.create(RpcInterface.class);
		ServiceDefTarget t = (ServiceDefTarget) async;
		t.setServiceEntryPoint(path);
	}
	
	public static RpcInterfaceAsync getRpcInterface () {
		return async;
	}
}
