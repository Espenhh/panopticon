(function() {
    var url = window.location.href.includes('localhost') ? 'http://localhost:8080/api' : '/api';
    var app = Elm.Main.fullscreen({
        url: url
    });
})();
