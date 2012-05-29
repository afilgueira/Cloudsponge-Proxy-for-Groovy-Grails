package proxy

import org.codehaus.groovy.grails.web.util.WebUtils


class CloudspongeController {
	
	def proxy = {

        // append the incoming querystring to the CloudSponge auth endpoint
        String uri = "https://api.cloudsponge.com/auth?" + request.queryString
				        
		URL url = new URL(uri)
		HttpURLConnection connection = (HttpURLConnection) url.openConnection()
		connection.setRequestMethod(request.method)

		// Don't follow redirects, api.cloudsponge.com should return a redirect that should be passed to the client UA
		connection.setInstanceFollowRedirects(false);
		        
        if (request.method == "POST") {
			
			// format the form data as: name1=value1&name2=value2&..
			String bodyParameters = WebUtils.toQueryString(params).substring(1)
			
			// WindowsLive POSTs the token, so this page should POST to api.cloudsponge.com
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            byte[] bytes = bodyParameters.getBytes("ASCII")
            OutputStream os = null
			
            try { 
				// send the Post
				connection.setDoOutput(true)
				os = connection.getOutputStream()
				os.write(bytes)
            } catch (Exception ex) {
                println "An error occurred while importing contacts: " + ex.message
            } finally {
                if (os != null) {
                    os.close()
                }
            }
        }

        try {
            // get the response
            InputStream webResponse = connection.getInputStream()
            if (webResponse == null) {
                 println "An error occurred while importing contacts: null web response"
				 return
            }
			
            // typically, this should redirect
            if (connection.getHeaderFields()["Location"] != null) {
				redirect(url: connection.getHeaderFields()["Location"][0])
				return // redirect and end processing now, nothing after this line will be executed.
            }
			
			println "An error occurred while importing contacts: null location"
			return
        }
        catch (Exception ex) {
            println "An error occurred while importing contacts: " + ex.message
			return
        }
    }
}
