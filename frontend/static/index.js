window.addEventListener('load', function() {

    var tokenRenewalTimeout
    var url = window.location.href.includes('localhost') ? 'http://localhost:8080/api' : '/api';

    var webAuth = new auth0.WebAuth({
        domain: window.domain,
        clientID: window.clientId,
        responseType: 'token id_token',
        redirectUri: location.href,
    })

    function setSession(authResult) {
        var expiresAt = JSON.stringify(authResult.expiresIn * 1000 + new Date().getTime());

        localStorage.setItem('id_token', authResult.idToken);
        localStorage.setItem('expires_at', expiresAt);

        scheduleRenewal()
    }

    function clearSession() {
        localStorage.removeItem('id_token');
        localStorage.removeItem('expires_at');

        clearTimeout(tokenRenewalTimeout)
    }

    function scheduleRenewal() {
        var expiresAt = JSON.parse(localStorage.getItem('expires_at'));
        var delay = expiresAt - Date.now();

        if (delay > 0) {
            tokenRenewalTimeout = setTimeout(function() {
                renewToken();
            }, delay);
        }
    }

    function isAuthenticated() {
        var expiresAt = JSON.parse(localStorage.getItem('expires_at'));
        return new Date().getTime() < expiresAt && localStorage.getItem('id_token');
    }

    function renewToken() {
        webAuth.checkSession({}, function (err, authResult) {
            if (err) {
                console.log(err)
                clearSession()
                webAuth.authorize();
            } else {
                setSession(authResult)
                Elm.Main.fullscreen({
                    token: authResult.idToken,
                    url: url
                })
            }
        })
    }

    function handleAuthentication() {
        webAuth.parseHash({ hash: window.location.hash }, function(err, authResult) {
            if (authResult && authResult.idToken) {
                console.log('Start with newly created token')
                setSession(authResult)
                Elm.Main.fullscreen({
                    token: authResult.idToken,
                    url: url
                })
            } else if (err) {
                console.log(err)
                clearSession()
            } else {
                if (isAuthenticated()) {
                    console.log('Authenticated until', new Date(JSON.parse(localStorage.getItem('expires_at'))))
                    scheduleRenewal()
                    Elm.Main.fullscreen({
                        token: localStorage.getItem('id_token'),
                        url: url

                    })
                } else {
                    console.log('No token, attempting to retrieve one')
                    clearSession()
                    webAuth.authorize()
                }
            }
        })
    }

    handleAuthentication()
})
