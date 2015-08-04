package com.qileyuan.tatala.example.proxy;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.google.protobuf.InvalidProtocolBufferException;
import com.qileyuan.tatala.example.service.model.Account;
import com.qileyuan.tatala.example.service.model.AllTypeBean;
import com.qileyuan.tatala.example.service.model.proto.AccountProto;
import com.qileyuan.tatala.example.service.model.wrapper.AccountListWrapper;
import com.qileyuan.tatala.example.service.model.wrapper.AccountMapWrapper;
import com.qileyuan.tatala.example.service.model.wrapper.AccountWrapper;
import com.qileyuan.tatala.example.service.model.wrapper.AllTypeBeanWrapper;
import com.qileyuan.tatala.executor.ServerExecutor;
import com.qileyuan.tatala.socket.to.TransferObject;
import com.qileyuan.tatala.socket.to.TransferObjectFactory;

/**
 * This class is a sample for the socket server provider. It is a proxy class
 * which call real business logic through socket connection.
 * 
 * Follow these steps: 
 * 1)Create different TransferObjectFactory by passing different parameter. 
 * Parameter is socket connection name present different socket server.
 *  
 * 2)Create caller method running in client side. Create transfer
 * object by TransferObjectFactory. Set callee class, callee method and return
 * type. Put parameter into transfer object. Call ServerExecutor. 
 * 
 * @author JimT
 * 
 */
public class ExampleClientProxy {

	private String IP = "127.0.0.1";
	private int PORT = 10001;
	private int TIMEOUT = 5000;
	
	private TransferObjectFactory transferObjectFactory;

	public ExampleClientProxy(){
		//create long connection factory
		transferObjectFactory = new TransferObjectFactory(IP, PORT, TIMEOUT);
	}
	
	public String sayHello(int Id, String name) {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("sayHello");
		to.registerReturnType(TransferObject.DATATYPE_STRING);

		to.putInt(Id);
		to.putString(name);

		Object resultObj = ServerExecutor.execute(to);
		String result = (String) resultObj;

		return result;
	}
		
	public void doSomething() {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("doSomething");
		to.registerReturnType(TransferObject.DATATYPE_VOID);

		ServerExecutor.execute(to);
	}
	
	public void exceptionCall(int Id) {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("exceptionCall");
		to.putInt(Id);
		to.registerReturnType(TransferObject.DATATYPE_VOID);

		ServerExecutor.execute(to);
	}

	public Account getAccount(Account account) throws Exception {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAccount");
		to.registerReturnType(TransferObject.DATATYPE_WRAPPER);

		AccountWrapper accountWrapper = new AccountWrapper(account);
		to.putWrapper(accountWrapper);

		accountWrapper = (AccountWrapper) ServerExecutor.execute(to);

		if(accountWrapper != null){
			Account returnAccount = accountWrapper.getAccount();
			return returnAccount;
		}else{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Account getAccountAsynchronous(Account account) throws Exception {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAccount");
		to.registerReturnType(TransferObject.DATATYPE_WRAPPER);

		AccountWrapper accountWrapper = new AccountWrapper(account);
		to.putWrapper(accountWrapper);

		to.setAsynchronous(true);

		Future<AccountWrapper> future = (Future<AccountWrapper>) ServerExecutor.execute(to);

		accountWrapper = future.get();
		Account returnAccount = accountWrapper.getAccount();

		return returnAccount;
	}

	public Account getAccountCompress(Account account) throws Exception {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAccount");
		to.registerReturnType(TransferObject.DATATYPE_WRAPPER);

		AccountWrapper accountWrapper = new AccountWrapper(account);
		to.putWrapper(accountWrapper);
		to.setCompress(true);

		accountWrapper = (AccountWrapper) ServerExecutor.execute(to);
		Account returnAccount = accountWrapper.getAccount();

		return returnAccount;
	}

	public Account getAccountDefaultProxy(Account account) {
		TransferObject to = transferObjectFactory.createTransferObject();
		//don't set callee class, handle with default server proxy
		//to.setCalleeClass("com.qileyuan.tatala.example.proxy.TestServerProxy");
		to.setDefaultCallee(true);
		to.setCalleeMethod("getAccount");
		to.registerReturnType(TransferObject.DATATYPE_WRAPPER);

		AccountWrapper accountWrapper = new AccountWrapper(account);
		to.putWrapper(accountWrapper);

		accountWrapper = (AccountWrapper) ServerExecutor.execute(to);
		if (accountWrapper == null) {
			return null;
		}
		Account returnAccount = accountWrapper.getAccount();

		return returnAccount;
	}

	public Account getAccountSerializable(Account account) {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAccountSerializable");
		to.registerReturnType(TransferObject.DATATYPE_SERIALIZABLE);
		to.putSerializable(account);

		Account returnAccount = (Account) ServerExecutor.execute(to);

		return returnAccount;
	}

	public List<Account> getAccountList(List<Account> accountList) {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAccountList");
		to.registerReturnType(TransferObject.DATATYPE_WRAPPER);

		AccountListWrapper testAccountListWrapper = new AccountListWrapper(accountList);
		to.putWrapper(testAccountListWrapper);
		testAccountListWrapper = (AccountListWrapper) ServerExecutor.execute(to);

		if(testAccountListWrapper != null){
			List<Account> retaccountList = testAccountListWrapper.getTestAccountList();
			return retaccountList;
		}else{
			return null;
		}
	}
	
	public Map<String, Account> getAccountMap(Map<String, Account> accountMap) {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAccountMap");
		to.registerReturnType(TransferObject.DATATYPE_WRAPPER);

		AccountMapWrapper testAccountMapWrapper = new AccountMapWrapper(accountMap);
		to.putWrapper(testAccountMapWrapper);
		testAccountMapWrapper = (AccountMapWrapper) ServerExecutor.execute(to);

		if(testAccountMapWrapper != null){
			Map<String, Account> retaccountMap = testAccountMapWrapper.getTestAccountMap();
			return retaccountMap;
		}else{
			return null;
		}
	}
	
	public AllTypeBean getAllTypeBean(boolean aboolean, byte abyte,
			short ashort, char achar, int aint, long along, float afloat,
			double adouble, Date adate, String astring) {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAllTypeBean");
		to.registerReturnType(TransferObject.DATATYPE_WRAPPER);

		to.putBoolean(aboolean);
		to.putByte(abyte);
		to.putShort(ashort);
		to.putChar(achar);
		to.putInt(aint);
		to.putLong(along);
		to.putFloat(afloat);
		to.putDouble(adouble);
		to.putDate(adate);
		to.putString(astring);

		AllTypeBeanWrapper allTypeBeanWrapper = (AllTypeBeanWrapper) ServerExecutor.execute(to);
		if(allTypeBeanWrapper != null){
			return allTypeBeanWrapper.getAllTypeBean();
		}else{
			return null;
		}
		
	}

	public String[] getArray(byte[] bytearr, String[] strarr) {
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getArray");
		to.registerReturnType(TransferObject.DATATYPE_STRINGARRAY);

		to.putByteArray(bytearr);
		to.putStringArray(strarr);

		Object resultObj = ServerExecutor.execute(to);
		String[] result = (String[]) resultObj;

		return result;
	}
	
	public Account getAccountProto (Account account) throws InvalidProtocolBufferException{
		TransferObject to = transferObjectFactory.createTransferObject();
		to.setCalleeClass("com.qileyuan.tatala.example.proxy.ExampleServerProxy");
		to.setCalleeMethod("getAccountProto");
		to.registerReturnType(TransferObject.DATATYPE_BYTEARRAY);

		AccountProto.Account.Builder accountProtoBuilder = AccountProto.Account.newBuilder();
		accountProtoBuilder.setId(account.getId());
		accountProtoBuilder.setName(account.getName());
		to.putByteArray(accountProtoBuilder.build().toByteArray());

		byte[] returnByteArray = (byte[]) ServerExecutor.execute(to);

		AccountProto.Account accountProto = AccountProto.Account.parseFrom(returnByteArray);
		if(accountProto != null){
			Account returnAccount = new Account();
			returnAccount.setId(accountProto.getId());
			returnAccount.setName(accountProto.getName());
			returnAccount.setAddress(accountProto.getAddress());
			return returnAccount;
		}else{
			return null;
		}
	}
}
