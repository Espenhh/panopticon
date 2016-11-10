module Main exposing (..)

import Html exposing (..)
import Html.App exposing (program, map)
import Http
import Task exposing (Task)
import Component.Model
import Component.Update
import Component.Messages
import Component.Decoder
import Component.View
import List
import Debug exposing (log)


type alias Model =
    { components : List Component.Model.Model
    }


init : ( Model, Cmd Msg )
init =
    ( Model [], getSystemStatus )


type Msg
    = GetSystemStatus
    | GetFailed Http.Error
    | GetSucceeded (List Component.Model.Model)
    | ComponentMsg Component.Messages.Msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GetSystemStatus ->
            ( model, getSystemStatus )

        GetFailed error ->
            log (toString error) ( model, Cmd.none )

        GetSucceeded components ->
            ( { model | components = components }, Cmd.none )

        ComponentMsg componentMsg ->
            let
                componentModels =
                    List.map (Component.Update.update componentMsg) model.components
            in
                ( { model | components = componentModels }, Cmd.none )


getSystemStatus : Cmd Msg
getSystemStatus =
    Task.perform GetFailed GetSucceeded getSystemStatusRequest


getSystemStatusRequest : Task Http.Error (List Component.Model.Model)
getSystemStatusRequest =
    Http.get Component.Decoder.listDecoder "/systemstatus.json"


view : Model -> Html Msg
view model =
    let
        components =
            List.map viewComponent model.components
    in
        div [] components


viewComponent : Component.Model.Model -> Html Msg
viewComponent model =
    Html.App.map ComponentMsg (Component.View.view model)


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none


main : Program Never
main =
    program { init = init, update = update, view = view, subscriptions = subscriptions }
