package com.btr.proxy.selector.misc;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.btr.proxy.selector.direct.NoProxySelector;

/*****************************************************************************
 * This is a facade for a list of ProxySelecor objects. You can register 
 * different ProxySelectors per Protocol.
 * 
 * @author Bernd Rosstauscher (proxyvole@rosstauscher.de) Copyright 2009
 ****************************************************************************/

public class ProtocolDispatchSelector extends ProxySelector {
	
	private Map<String, ProxySelector> selectors;
	private ProxySelector fallbackSelector;
	
	/*************************************************************************
	 * Constructor
	 ************************************************************************/
	
	public ProtocolDispatchSelector() {
		super();
		this.selectors = new ConcurrentHashMap<String, ProxySelector>();
		this.fallbackSelector = NoProxySelector.getInstance();
	}
	
	/*************************************************************************
	 * Sets a selector responsible for the given protocol.
	 * @param protocol the name of the protocol.
	 * @param selector the selector to use.
	 ************************************************************************/
	
	public void setSelector(String protocol, ProxySelector selector) {
		if (protocol == null) {
			throw new NullPointerException("Protocol must not be null.");
		}
		if (selector == null) {
			throw new NullPointerException("Selector must not be null.");
		}
		this.selectors.put(protocol, selector);
	}
	
	/*************************************************************************
	 * Removes the selector installed for the given protocol.
	 * @param protocol the protocol name.
	 * @return the old selector that is removed.
	 ************************************************************************/
	
	public ProxySelector removeSelector(String protocol) {
		return this.selectors.remove(protocol);
	}
	
	/*************************************************************************
	 * Gets the selector installed for the given protocol.
	 * @param protocol the protocol name.
	 * @return the selector for that protocol, null if none is currently set.
	 ************************************************************************/
	
	public ProxySelector getSelector(String protocol) {
		return this.selectors.get(protocol);
	}
	
	/*************************************************************************
	 * Sets the fallback selector that is always called when no matching 
	 * protocol selector was found..
	 * @param selector the selector to use.
	 ************************************************************************/
	
	public void setFallbackSelector(ProxySelector selector) {
		if (selector == null) {
			throw new NullPointerException("Selector must not be null.");
		}
		this.fallbackSelector = selector;
	}
	
	/*************************************************************************
	 * connectFailed
	 * @see java.net.ProxySelector#connectFailed(java.net.URI, java.net.SocketAddress, java.io.IOException)
	 ************************************************************************/

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		String protocol = uri.getScheme();
		ProxySelector selector = this.selectors.get(protocol);
		if (selector == null) {
			selector = this.fallbackSelector;
		}
		selector.connectFailed(uri, sa, ioe);
	}

	/*************************************************************************
	 * select
	 * @see java.net.ProxySelector#select(java.net.URI)
	 ************************************************************************/

	@Override
	public List<Proxy> select(URI uri) {
		String protocol = uri.getScheme();
		ProxySelector selector = this.selectors.get(protocol);
		if (selector == null) {
			selector = this.fallbackSelector;
		}
		return selector.select(uri);
	}

}