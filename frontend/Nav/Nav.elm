module Nav.Nav exposing (..)

import Navigation
import UrlParser exposing (Parser, (</>), oneOf, format, s, string, parse)
import String exposing (dropLeft)
import App.Model exposing (Model, getSystemStatus)
import App.Messages exposing (Msg(..))
import Nav.Model exposing (Page(..))
import Nav.Requests exposing (getDetails)


toHash : Page -> String
toHash page =
    case page of
        Components ->
            "#"

        Component index ->
            "#component/" ++ index


hashParser : Navigation.Location -> Result String Page
hashParser location =
    parse identity pageParser (dropLeft 1 location.hash)


pageParser : Parser (Page -> a) a
pageParser =
    oneOf
        [ format Components (s "")
        , format Component (s "component" </> string)
        ]


urlUpdate : Result String Page -> Model -> ( Model, Cmd Msg )
urlUpdate result model =
    case result of
        Err a ->
            ( model, Navigation.modifyUrl (toHash model.page) )

        Ok (Components as page) ->
            ( { model | page = page }, getSystemStatus )

        Ok ((Component _) as page) ->
            let
                msg =
                    Cmd.map DetailMsg getDetails
            in
                ( { model | page = page }, msg )
