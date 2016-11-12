module Nav.Requests exposing (getDetails)

import Http
import Task exposing (Task)
import Detail.Messages
import Detail.Model
import Detail.Decoder


getDetails : Cmd Detail.Messages.Msg
getDetails =
    Task.perform Detail.Messages.GetFailed Detail.Messages.GetSucceeded getDetailsRequest


getDetailsRequest : Task Http.Error Detail.Model.Model
getDetailsRequest =
    Http.get Detail.Decoder.decoder "/details.json"
