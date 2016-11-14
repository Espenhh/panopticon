module Detail.Model exposing (Model, init)

import Metric.Model


type alias Model =
    { environment : String
    , system : String
    , component : String
    , server : String
    , measurements : List Metric.Model.Model
    }


init : Model
init =
    Model "" "" "" "" []
