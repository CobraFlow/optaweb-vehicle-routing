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
import { ActionGroup, Button, Form, FormGroup, TextInput } from '@patternfly/react-core';
import * as L from 'leaflet';
import * as React from 'react';
import { Marker, Popup, Tooltip } from 'react-leaflet';
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
  updateHandler: (location: Location) => void;
}

export interface State {
  description?: string;
}

class LocationMarker extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      description: props.location.description,
    };

    this.handleSave = this.handleSave.bind(this);
    this.handleRemove = this.handleRemove.bind(this);
    this.handleCancel = this.handleCancel.bind(this);
  }

  private icon = this.props.isDepot ? homeIcon : defaultIcon;

  handleSave() {
    const location: Location = {
      id: this.props.location.id,
      lat: this.props.location.lat,
      lng: this.props.location.lng,
      description: this.state.description,
    };
    this.props.updateHandler(location);
  }

  handleRemove() {
    this.props.removeHandler(this.props.location.id);
  }

  handleCancel() {
    console.log(this);
  }

  render() {
    const { description } = this.state;
    const { id, lat, lng } = this.props.location;

    return (
      <Marker
        key={id}
        position={this.props.location}
        icon={this.icon}
      >
        <Popup>
          <span>
            {`id ${id}, description=${description}`}
          </span>
          <br />
          <Form>
            <FormGroup
              fieldId="popup-form"
              label="Description"
            >
              <TextInput
                id="input-description"
                className="popup-input"
                type="text"
                value={description}
                onChange={(value) => {
                  this.setState({ description: value });
                }}
              />
            </FormGroup>
            <ActionGroup>
              <Button id="save-button" variant="primary" onClick={this.handleSave}>Save</Button>
              <Button id="remove-button" variant="link" onClick={this.handleRemove}>Remove</Button>
              <Button id="cancel-button" variant="link" onClick={this.handleCancel}>Cancel</Button>
            </ActionGroup>
          </Form>
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
          {`Location ${id}
          [Lat=${lat},
          Lng=${lng}]
          Desc=${description}`}
        </Tooltip>
      </Marker>
    );
  }
}

export default LocationMarker;
