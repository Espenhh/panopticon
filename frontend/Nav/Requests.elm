module Nav.Requests exposing (getDetails, getSystemStatus)

import Http exposing (empty, send, defaultSettings, fromJson)
import Task exposing (Task)
import App.Messages
import Components.Model
import Components.Decoder
import Detail.Messages
import Detail.Model
import Detail.Decoder
import Json.Decode exposing (Decoder)
import String


baseUrl : String
baseUrl =
    "http://localhost:8080/"


getSystemStatus : Cmd App.Messages.Msg
getSystemStatus =
    Task.perform App.Messages.GetFailed App.Messages.GetSucceeded getSystemStatusRequest


getDetails : String -> String -> String -> String -> Cmd Detail.Messages.Msg
getDetails env system component server =
    Task.perform Detail.Messages.GetFailed Detail.Messages.GetSucceeded <|
        getDetailsRequest env system component server


getSystemStatusRequest : Task Http.Error Components.Model.Model
getSystemStatusRequest =
    jsonGet "http://localhost:8080/internal/status" Components.Decoder.decoder


getDetailsRequest : String -> String -> String -> String -> Task Http.Error Detail.Model.Model
getDetailsRequest env system component server =
    let
        url =
            baseUrl ++ String.join "/" [ "internal", "status", env, system, component, server ]
    in
        jsonGet url Detail.Decoder.decoder


jsonGet : String -> Decoder a -> Task Http.Error a
jsonGet url decoder =
    fromJson decoder <|
        send defaultSettings
            { verb = "GET"
            , headers = [ ( "Accept", "application/json" ) ]
            , url = url
            , body = empty
            }
