/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package org.xidea.webwork.result;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <!-- START SNIPPET: description -->
 *
 * Calls the {@link HttpServletResponse#sendRedirect(String) sendRedirect}
 * method to the location specified. The response is told to redirect the
 * browser to the specified location (a new request from the client). The
 * consequence of doing this means that the action (action instance, action
 * errors, field errors, etc) that was just executed is lost and no longer
 * available. This is because actions are built on a single-thread model. The
 * only way to pass data is through the session or with web parameters
 * (url?name=value) which can be OGNL expressions.
 *
 * <!-- END SNIPPET: description -->
 * <p/>
 * <b>This result type takes the following parameters:</b>
 *
 * <!-- START SNIPPET: params -->
 *
 * <ul>
 *
 * <li><b>location (default)</b> - the location to go to after execution.</li>
 *
 * <li><b>parse</b> - true by default. If set to false, the location param will 
 * not be parsed for Ognl expressions.</li>
 *
 * </ul>
 *
 * <p>
 * This result follows the same rules from {@link WebworkResultSupport}.
 * </p>
 *
 * <!-- END SNIPPET: params -->
 *
 * <b>Example:</b>
 *
 * <pre><!-- START SNIPPET: example -->
 * &lt;result name="success" type="redirect"&gt;
 *   &lt;param name="location"&gt;foo.jsp&lt;/param&gt;
 *   &lt;param name="parse"&gt;false&lt;/param&gt;
 * &lt;/result&gt;
 * <!-- END SNIPPET: example --></pre>
 *
 * @author Patrick Lightbody
 */
public class ServletRedirectResult extends WebworkResultSupport {
	
	private static final long serialVersionUID = -4787182693920514194L;

	private static final Log log = LogFactory.getLog(ServletRedirectResult.class);

    protected boolean prependServletContext = true;

    /**
     * Sets whether or not to prepend the servlet context path to the redirected URL.
     *
     * @param prependServletContext <tt>true</tt> to prepend the location with the servlet context path,
     *                              <tt>false</tt> otherwise.
     */
    public void setPrependServletContext(boolean prependServletContext) {
        this.prependServletContext = prependServletContext;
    }

    /**
     * Redirects to the location specified by calling {@link HttpServletResponse#sendRedirect(String)}.
     *
     * @param finalLocation the location to redirect to.
     * @param invocation    an encapsulation of the action execution state.
     * @throws Exception if an error occurs when redirecting.
     */
    public void execute(ActionInvocation invocation) throws Exception {
        ActionContext ctx = invocation.getInvocationContext();
        HttpServletRequest request = (HttpServletRequest) ctx.get(HttpServletRequest.class);
        HttpServletResponse response = (HttpServletResponse) ctx.get(HttpServletResponse.class);

        String finalLocation = parseParams(location, invocation);
        if (isPathUrl(finalLocation)) {
            if (!finalLocation.startsWith("/")) {
            	//TODO:???
                String namespace = invocation.getProxy().getNamespace();

                if ((namespace != null) && (namespace.length() > 0) && (!"/".equals(namespace))) {
                    finalLocation = namespace + "/" + finalLocation;
                } else {
                    finalLocation = "/" + finalLocation;
                }
            }

            // if the URL's are relative to the servlet context, append the servlet context path
            if (prependServletContext && (request.getContextPath() != null) && (request.getContextPath().length() > 0)) {
                finalLocation = request.getContextPath() + finalLocation;
            }

            finalLocation = response.encodeRedirectURL(finalLocation);
        }

        if (log.isDebugEnabled()) {
            log.debug("Redirecting to finalLocation " + finalLocation);
        }

        response.sendRedirect(finalLocation);
    }

    private static boolean isPathUrl(String url) {
        // filter out "http:", "https:", "mailto:", "file:", "ftp:"
        // since the only valid places for : in URL's is before the path specification
        // either before the port, or after the protocol
        return (url.indexOf(':') == -1);
    }
}
