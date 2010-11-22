package project.core.mbeans.database;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.jboss.util.naming.NonSerializableFactory;

public class JndiBinder {

	private String jndiName = "";
	private boolean isBound = false;
	private Object objToBind = null;
	
	public JndiBinder () {
		
		setObjectToBind(this);
	}
	
	public JndiBinder (String jndiName) {
		
		setObjectToBind(this);
		try {
			this.setJndiName(jndiName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	protected void rebind() throws NamingException {
		if (!isBound) {
			InitialContext rootCtx = new InitialContext();
			try {
				Name fullName = rootCtx.getNameParser("").parse(jndiName);
				System.out.println("Bound to: " + fullName);
				NonSerializableFactory.rebind(fullName, getObjectToBind(), true);
				isBound = true;
			} finally {
				rootCtx.close(); 
			}
		}
	}
	
	protected void unbind () throws NamingException {
		this.unBind(jndiName);
	}

	protected void unBind(String jndiName) throws NamingException {
		if (isBound) {
			InitialContext rootCtx = new InitialContext();
			try {
				rootCtx.unbind(jndiName);
				NonSerializableFactory.unbind(jndiName);
				isBound = false;
			} finally {
				rootCtx.close(); 
			}
		}
	}

	protected Object getObjectToBind() {
		return objToBind;
	}
	
	public void setObjectToBind (Object o) {
		objToBind = o;
	}

	public String getJndiName() {
		return jndiName;
	}
	
	public void setJndiName (String jndiName) {
		this.setJndiName(jndiName, true);
	}

	public void setJndiName(String jndiName, boolean autoRebind) {
		
		try {
			this.unBind(jndiName);
			this.jndiName = jndiName;
			this.rebind();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	
	public static void bind (String jndiName, Object o) {
		
		JndiBinder binder = new JndiBinder ();
		binder.setObjectToBind(o);
		binder.setJndiName(jndiName);
		
	}
	
	public static void unbind (String jndiName) {
		
		JndiBinder binder = new JndiBinder ();
		try {
			binder.unBind(jndiName);
		} catch (NamingException e) { e.printStackTrace(); }
		
	}
}
