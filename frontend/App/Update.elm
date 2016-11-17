module App.Update exposing (update)

import App.Messages exposing (..)
import App.Model exposing (..)
import Components.Update
import Detail.Update
import Nav.Requests exposing (getSystemStatus, getDetails)
import Nav.Model exposing (..)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateUrl (Components as page) ->
            ( { model | page = page }, getSystemStatus )

        UpdateUrl ((Component env system component server) as page) ->
            let
                cmd =
                    Cmd.map DetailMsg <| getDetails env system component server
            in
                ( { model | page = page }, cmd )

        GetSystemStatus ->
            ( model, getSystemStatus )

        SystemStatus (Err _) ->
            ( model, Cmd.none )

        SystemStatus (Ok components) ->
            ( { model | components = components }, Cmd.none )

        ComponentsMsg componentsMsg ->
            let
                componentsModel =
                    Components.Update.update componentsMsg model.components
            in
                ( { model | components = componentsModel }, Cmd.none )

        DetailMsg detailMsg ->
            let
                detailModel =
                    Detail.Update.update detailMsg model.detail
            in
                ( { model | detail = detailModel }, Cmd.none )
