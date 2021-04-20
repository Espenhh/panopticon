module Detail.Update exposing (update)

import Detail.Messages exposing (..)
import Detail.Model exposing (..)
import Nav.Requests exposing (getDetails)
import Ports


update : Maybe String -> Msg -> Model -> ( Model, Cmd Msg )
update token msg model =
    case msg of
        Update ->
            ( model, Cmd.none )

        GetDetails baseUrl ->
            let
                request =
                    getDetails baseUrl token model.environment model.system model.component model.server
            in
            ( model, request )

        Get (Err err) ->
            ( model
            , Ports.log ("Klarte ikke å dekode detaljer-kallet: " ++ toString err)
            )

        Get (Ok metrics) ->
            ( metrics, Cmd.none )
