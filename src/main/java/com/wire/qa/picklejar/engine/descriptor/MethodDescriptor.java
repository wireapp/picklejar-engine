package com.wire.qa.picklejar.engine.descriptor;

import java.lang.reflect.Method;

public class MethodDescriptor {

    private String text;
    private Method method;
    private Object[] parameters;

    public MethodDescriptor(Method method, Object[] parameters) {
        this.method = method;
        this.parameters = parameters;
	}

	public void setMethod(Method method) {
        this.method = method;
	}

	public Method getMethod() {
		return this.method;
    }
    
    public Object[] getParameters() {
        return this.parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
	}
}