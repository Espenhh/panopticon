module Nav.Nav exposing (..)

import Navigation
import UrlParser exposing (Parser, (</>), oneOf, format, s, int, parse)
import String exposing (dropLeft)
import App.Model exposing (Model)
import App.Messages exposing (Msg)
import Nav.Model exposing (Page(..))


toHash : Page -> String
toHash page =
    case page of
        Components ->
            "#"

        Component index ->
            "#component/" ++ toString index


hashParser : Navigation.Location -> Result String Page
hashParser location =
    parse identity pageParser (dropLeft 1 location.hash)


pageParser : Parser (Page -> a) a
pageParser =
    oneOf
        [ format Components (s "")
        , format Component (s "component" </> int)
        ]


urlUpdate : Result String Page -> Model -> ( Model, Cmd Msg )
urlUpdate result model =
    case result of
        Err a ->
            ( model, Navigation.modifyUrl (toHash model.page) )

        Ok newPage ->
            { model | page = newPage } ! []
