(function() {
    var app;
    var url = window.location.href.includes('localhost') ? 'http://localhost:8080/api' : '/api';
    var lock = new Auth0Lock(window.clientId, window.domain, {
        auth: {
            autoParseHash: false
        }
    });

    lock.resumeAuth(window.location.hash, function(err, result) {
        if (err) {
            console.log(err);
            return;
        }

        var savedToken = localStorage.getItem('login_token');

        if (result) {
            var token = result.idToken;
            localStorage.setItem('login_token', token);
            app = Elm.Main.fullscreen({
                url: url,
                token: token
            });

        } else if (savedToken) {
            app = Elm.Main.fullscreen({
                url: url,
                token: savedToken
            });
        } else {
            app = Elm.Main.fullscreen({
                url: url,
                token: ''
            });
        }
    });

    app.ports.login.subscribe(function() {
        lock.show();
    });

})();
