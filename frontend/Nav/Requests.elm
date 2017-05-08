module Nav.Requests exposing (getDetails, getSystemStatus)

import Http
import App.Messages
import Components.Decoder
import Detail.Messages
import Detail.Decoder
import Json.Decode exposing (Decoder)
import String


handle403 : Cmd App.Messages.Msg -> Cmd App.Messages.Msg
handle403 msg =
    flip Cmd.map msg <|
        (\result ->
            case result of
                App.Messages.SystemStatus (Err err) ->
                    case err of
                        Http.BadStatus response ->
                            if response.status.code == 403 then
                                App.Messages.Login
                            else
                                result

                        _ ->
                            result

                App.Messages.SystemStatus (Ok res) ->
                    App.Messages.SystemStatus (Result.Ok res)

                _ ->
                    result
        )


getSystemStatus : String -> Maybe String -> Cmd App.Messages.Msg
getSystemStatus baseUrl token =
    handle403 <|
        Http.send App.Messages.SystemStatus <|
            jsonGet Components.Decoder.decoder token <|
                url [ baseUrl, "internal", "status" ]


getDetails : String -> Maybe String -> String -> String -> String -> String -> Cmd Detail.Messages.Msg
getDetails baseUrl token env system component server =
    Http.send Detail.Messages.Get <|
        jsonGet Detail.Decoder.decoder token <|
            url [ baseUrl, "internal", "status", env, system, component, server ]


url : List String -> String
url =
    String.join "/"


jsonGet : Decoder a -> Maybe String -> String -> Http.Request a
jsonGet decoder token url =
    Http.request
        { method = "GET"
        , headers = [ Http.header "Accept" "application/json", Http.header "authorization" (Maybe.withDefault "" token) ]
        , url = url
        , body = Http.emptyBody
        , expect = Http.expectJson decoder
        , timeout = Nothing
        , withCredentials = False
        }
