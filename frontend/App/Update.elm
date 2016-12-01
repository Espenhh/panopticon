module App.Update exposing (update)

import App.Messages exposing (..)
import App.Model exposing (..)
import Components.Update
import Detail.Update
import Detail.Model
import Nav.Requests exposing (getSystemStatus, getDetails)
import Nav.Model exposing (..)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateUrl page ->
            updatePage page model

        GetSystemStatus ->
            ( model, getSystemStatus model.flags.url )

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
                ( detailModel, cmd ) =
                    Detail.Update.update detailMsg model.detail

                mappedCmd =
                    Cmd.map DetailMsg cmd
            in
                ( { model | detail = detailModel }, mappedCmd )


updatePage : Page -> Model -> ( Model, Cmd Msg )
updatePage page m =
    let
        model =
            leftPage m.page m
    in
        case page of
            Components as page ->
                ( { model | page = page }, getSystemStatus model.flags.url )

            (Component env system component server) as page ->
                let
                    cmd =
                        Cmd.map DetailMsg <| getDetails model.flags.url env system component server
                in
                    ( { model | page = page }, cmd )


leftPage : Page -> Model -> Model
leftPage oldPage model =
    case oldPage of
        Component _ _ _ _ ->
            { model | detail = Detail.Model.init }

        _ ->
            model
