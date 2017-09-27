package com.layer.ui.util;


import android.net.Uri;
import android.support.v7.widget.RecyclerView;

import com.layer.sdk.LayerDataObserver;
import com.layer.sdk.LayerDataRequest;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.LayerObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link LayerDataObserver} that looks for changes to identities that are bound to
 * view holders in a {@link RecyclerView.Adapter}, and trigger updates in the adapter accordingly.
 */
public class IdentityRecyclerViewEventListener implements LayerDataObserver.Weak {
    private final RecyclerView.Adapter mAdapter;
    private final Map<Uri, Set<Integer>> identityPositions = new HashMap<>();

    public IdentityRecyclerViewEventListener(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Set identities associated with the position in the adapter.
     *
     * @param position Position in the adapter that the identities are bound to
     * @param participants Identities to check for updates. Only the Uri of the identity
     *                     is stored.
     */
    public void addIdentityPosition(int position, Set<Identity> participants) {
        for (Identity participant : participants) {
            Set<Integer> positions = identityPositions.get(participant.getId());
            if (positions == null) {
                positions = new HashSet<>();
                identityPositions.put(participant.getId(), positions);
            }
            positions.add(position);
        }
    }

    @Override
    public void onDataChanged(LayerChangeEvent event) {
        for (LayerChange change : event.getChanges()) {
            if (change.getObjectType().equals(LayerObject.Type.IDENTITY)) {
                Uri id = ((Identity) change.getObject()).getId();
                Set<Integer> positions = identityPositions.get(id);
                if (positions != null) {
                    for (Integer position : positions) {
                        mAdapter.notifyItemChanged(position);
                    }
                }
            }
        }
    }

    @Override
    public void onDataRequestCompleted(LayerDataRequest request, LayerObject object) {

    }
}
