module Nav.Requests exposing (getDetails, getSystemStatus)

import Http
import App.Messages
import Components.Decoder
import Detail.Messages
import Detail.Decoder
import Json.Decode exposing (Decoder)
import String


getSystemStatus : String -> Cmd App.Messages.Msg
getSystemStatus baseUrl =
    Http.send App.Messages.SystemStatus <|
        jsonGet Components.Decoder.decoder <|
            url [ baseUrl, "internal", "status" ]


getDetails : String -> String -> String -> String -> String -> Cmd Detail.Messages.Msg
getDetails baseUrl env system component server =
    Http.send Detail.Messages.Get <|
        jsonGet Detail.Decoder.decoder <|
            url [ baseUrl, "internal", "status", env, system, component, server ]


url : List String -> String
url =
    String.join "/"


jsonGet : Decoder a -> String -> Http.Request a
jsonGet decoder url =
    Http.request
        { method = "GET"
        , headers = [ Http.header "Accept" "application/json" ]
        , url = url
        , body = Http.emptyBody
        , expect = Http.expectJson decoder
        , timeout = Nothing
        , withCredentials = False
        }
