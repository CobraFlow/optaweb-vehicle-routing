/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import '@patternfly/patternfly/patternfly.css';
import { TextInput } from '@patternfly/react-core';
import * as L from 'leaflet';
import * as React from 'react';
import { Marker, Tooltip, Popup } from 'react-leaflet';
import { Location } from 'store/route/types';

const homeIcon = L.icon({
  iconAnchor: [12, 12],
  iconSize: [24, 24],
  iconUrl: 'if_big_house-home_2222740.png',
  popupAnchor: [0, -10],
  shadowAnchor: [16, 2],
  shadowSize: [50, 16],
  shadowUrl: 'if_big_house-home_2222740_shadow.png',
});

const defaultIcon = new L.Icon.Default();

export interface Props {
  location: Location;
  isDepot: boolean;
  isSelected: boolean;
  removeHandler: (id: number) => void;
}

export interface State {
  valueArc: number;
  valueSpeed: number;
}

class LocationMarker extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      valueArc: 0,
      valueSpeed: 0,
    };
  }

  private icon = this.props.isDepot ? homeIcon : defaultIcon;

  render() {
    return (
      <Marker
        key={this.props.location.id}
        position={this.props.location}
        icon={this.icon}
      >
        <Popup>
          <span>
            {`Location ${this.props.location.id}, valueArc=${this.state.valueArc}, valueSpeed=${this.state.valueSpeed}`}
          </span>
          <br />
          <form id="popup-form">
            <TextInput
              id="input-speed"
              className="popup-input"
              type="number"
              label="New speed:"
            />
            <table className="popup-table">
              <tr className="popup-table-row">
                <th className="popup-table-header">Arc number:</th>
                <td id="value-arc" className="popup-table-data"> </td>
              </tr>
              <tr className="popup-table-row">
                <th className="popup-table-header">Current speed:</th>
                <td id="value-speed" className="popup-table-data"> </td>
              </tr>
            </table>
            <button id="button-submit" type="button">Save Changes</button>
          </form>
        </Popup>
        <Tooltip
          // `permanent` is a static property (this is a React-Leaflet-specific
          // approach: https://react-leaflet.js.org/docs/en/components). Changing `permanent` prop
          // doesn't result in calling `setPermanent()` on the Leaflet element after the Tooltip component is mounted.
          // We're using `key` to force re-rendering of Tooltip when `isSelected` changes. A similar use case for
          // the `key` property is described here:
          // https://reactjs.org/blog/2018/06/07/you-probably-dont-need-derived-state.html
          // #recommendation-fully-uncontrolled-component-with-a-key
          key={this.props.isSelected ? 'selected' : ''}
          permanent={this.props.isSelected}
        >
          {`Location ${this.props.location.id} [Lat=${this.props.location.lat}, Lng=${this.props.location.lng}]`}
        </Tooltip>
      </Marker>
    );
  }
}

export default LocationMarker;
