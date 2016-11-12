module Detail.Model exposing (Model, init)

import Metric.Model


type alias Model =
    { metrics : List Metric.Model.Model
    }


init : Model
init =
    Model []
