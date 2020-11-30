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

import * as React from 'react';
import { Popup } from 'react-leaflet';
import { ActionGroup, Button, Form, FormGroup, TextInput } from '@patternfly/react-core';
import { Location } from '../../store/route/types';

export interface Props {
  location: Location;
  removeHandler: (id: number) => void;
  updateHandler: (location: Location) => void;
  cancelHandler: () => void;
}

export interface State {
  description?: string;
}

class LocationPopup extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      description: props.location.description,
    };

    this.handleSave = this.handleSave.bind(this);
    this.handleRemove = this.handleRemove.bind(this);
    this.handleCancel = this.handleCancel.bind(this);
  }

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
    this.props.cancelHandler();
  }

  render() {
    const { description } = this.state;
    const { id } = this.props.location;

    return (
      <Popup
        minWidth={300}
      >
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
    );
  }
}

export default LocationPopup;
