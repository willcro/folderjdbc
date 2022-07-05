package com.willcro.folderdb.entity;

import com.willcro.folderdb.tester.UpdateState;
import lombok.Data;

@Data
public class FolderDbFile {

    private String filename;
    private String updateType;
    private String updateValue;

    public UpdateState getUpdateState() {
        return new UpdateState(updateType, updateValue);
    }

}
