module Nav.Nav exposing (..)

import Navigation
import UrlParser exposing (Parser, (</>), oneOf, format, s, string, parse)
import String exposing (dropLeft)
import App.Model exposing (Model)
import App.Messages exposing (Msg(..))
import Nav.Model exposing (Page(..))
import Nav.Requests exposing (getDetails, getSystemStatus)


toHash : Page -> String
toHash page =
    case page of
        Components ->
            "#"

        Component env system component server ->
            "#component/" ++ env ++ "/" ++ system ++ "/" ++ component ++ "/" ++ server


hashParser : Navigation.Location -> Result String Page
hashParser location =
    parse identity pageParser (dropLeft 1 location.hash)


pageParser : Parser (Page -> a) a
pageParser =
    oneOf
        [ format Components (s "")
        , format Component (s "component" </> string </> string </> string </> string)
        ]


urlUpdate : Result String Page -> Model -> ( Model, Cmd Msg )
urlUpdate result model =
    case result of
        Err a ->
            ( model, Navigation.modifyUrl (toHash model.page) )

        Ok (Components as page) ->
            ( { model | page = page }, getSystemStatus )

        Ok ((Component env system component server) as page) ->
            let
                msg =
                    Cmd.map DetailMsg <| getDetails env system component server
            in
                ( { model | page = page }, msg )
