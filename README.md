# Layer UI

## <a name="overview"></a>Overview

Layer UI is an open source framework of customizable UI components for use with the Layer SDK designed to get messaging tested and integrated quickly.  This repository contains the Layer UI library.  For a fully-featured messaging app, see the open source [Atlas Messenger](https://github.com/layerhq/Atlas-Android-Messenger) project, which uses this Layer UI library and the Layer SDK.

#### Requirements

Layer UI requires Android API Level >= 14 (OS v4.0). The Layer SDK version requirements for each release are tightly coupled. See the release notes for details about specifics.

## <a name="key_concepts"></a>Key Concepts
With Layer UI, Messages have types.  One type might be rich text, and another might be a map location or photo.  Anything that can be packaged into a set of MIME Types and data can be represented by Layer UI.

Under the hood, <a href="layer-atlas/src/main/java/com/layer/ui/message/messagetypes/MessageSender.java">MessageSenders</a> send individual Message types, and <a href="llayer-atlas/src/main/java/com/layer/ui/message/messagetypes/CellFactory.java">CellFactories</a> render them.  Additional Message types can be added to your app by extending these classes.  For a list of default types, see the <a href="layer-atlas/src/main/java/com/layer/ui/message/messagetypes">messagetypes</a> subpackage.

## <a name="api_quickstart"></a>API Quickstart
The Layer UI library is located in the `layer-atlas` directory.  The table below details the most important classes in Atlas and is hyperlinked directly to the current java file.

<table>
    <tr><th colspan="2" style="text-align:center;">Views</th></tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/conversation/ConversationItemsListView.java">ConversationItemsListView</a></td>
        <td>A list of Conversations</td>
    </tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/message/MessageItemsListView.java">MessageItemsListView</a></td>
        <td>A list of Messages within a Conversation</td>
    </tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/composebar/ComposeBar.java">ComposeBar</a></td>
        <td>A View used to compose and send Messages</td>
    </tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/AddressBar.java">AddressBar</a></td>
        <td>Participant selection with dynamic filtering</td>
    </tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/TypingIndicatorLayout.java">TypingIndicatorLayout</a></td>
        <td>Displays TypingIndicator information for a Conversation</td>
    </tr>
    <tr><th colspan="2" style="text-align:center;">Factories and Senders</th></tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/message/messagetypes/CellFactory.java">CellFactory</a></td>
        <td>Classifies, parses, and renders Messages</td>
    </tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/message/messagetypes/MessageSender.java">MessageSender</a></td>
        <td>Sends Messages</td>
    </tr>
    <tr>
        <td><a href="layer-atlas/src/main/java/com/layer/ui/TypingIndicatorLayout.java">TypingIndicatorLayout. TypingIndicatorFactory</a></td>
        <td>Renders typing indicators</td>
    </tr>
</table>

## <a name="installation"></a>Installation

Add the following to the `build.gradle`:
```groovy
repositories {
    maven { url "https://raw.githubusercontent.com/layerhq/releases-android/master/releases/" }
    maven { url "https://raw.githubusercontent.com/layerhq/Atlas-Android/master/releases/" }
}

dependencies {
    compile 'com.layer.atlas:layer-atlas:0.4.6'
}
```

### <a name="libraries"></a>Libraries

Layer UI uses [Picasso](https://github.com/square/picasso) for image caching, resizing, and processing, and [Subsampling Scale Image View](https://github.com/davemorrissey/subsampling-scale-image-view) for image its in-app lightbox.  Other dependencies include the Android `recyclerview`, `appcompat`, and `design` libraries.

## <a name="component_details"></a>Component Details
Layer UI is divided into five basic `View` components, typically presented on a screen with a user's [conversations](#conversations), a screen with [messages](#messages) within a conversation, and a component that lets the user select [participants](#participants).

### <a name="conversations"></a>Conversations

#### ConversationItemsListView

The <a href="layer-atlas/src/main/java/com/layer/ui/conversation/ConversationItemsListView.java">ConversationItemsListView</a> is a list of Conversations.

##### XML

It uses databinding which takes an object of <a href="layer-atlas/src/main/java/com/layer/ui/conversation/ConversationItemsListViewModel.java">ConversationItemsListViewModel</a> as a ViewModel. The ConversationItemsListViewModel creates an object of <a href="layer-atlas/src/main/java/com/layer/ui/adapters/ConversationItemsAdapter.java">ConversationItemsAdapter</a> which is passed as the Adapter in the xml.

        mConversationItemsAdapter = new ConversationItemsAdapter(context, layerClient, query,

```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>

        <import type="com.layer.ui.conversation.ConversationItemsListViewModel"/>

        <variable
            name="viewModel"
            type="ConversationItemsListViewModel"/>
    </data>

 <com.layer.ui.conversation.ConversationItemsListView
            android:id="@+id/conversations_list"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:itemHeight="@dimen/layer_ui_item_height_large"
            app:adapter="@{viewModel.conversationItemsAdapter}"
            app:itemSwipeListener = "@{viewModel.itemSwipeListener}"/>
</layout>
```



##### Java

Creates an Object of ConversationItemsListViewModel and binds it to the view.

```java
    mConversationsList = binding.conversationsList;

        mConversationItemsListViewModel = new ConversationItemsListViewModel(this, App.getLayerClient(), Util.getConversationItemFormatter(), Util.getImageCacheWrapper(),new IdentityFormatterImpl());

        mConversationItemsListViewModel.setItemClickListener(new OnItemClickListener<Conversation>() {
            @Override
            public void onItemClick(Conversation item) {
                Intent intent = new Intent(ConversationsListActivity.this, MessagesListActivity.class);
                if (Log.isLoggable(Log.VERBOSE)) {
                    Log.v("Launching MessagesListActivity with existing conversation ID: " + item.getId());
                }
                intent.putExtra(PushNotificationReceiver.LAYER_CONVERSATION_KEY, item.getId());
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(Conversation item) {
                return false;
            }
        });

        mConversationItemsListViewModel.setItemSwipeListener(new SwipeableItem.OnItemSwipeListener<Conversation>() {
            @Override
            public void onSwipe(final Conversation conversation, int direction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConversationsListActivity.this)
                        .setMessage(R.string.alert_message_delete_conversation)
                        .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConversationItemsAdapter adapter = mConversationItemsListViewModel.getConversationItemsAdapter();
                                // TODO: simply update this one message
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.alert_button_delete_all_participants, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                conversation.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
                            }
                        });
                // User delete is only available if read receipts are enabled
                if (conversation.isReadReceiptsEnabled()) {
                    builder.setNeutralButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            conversation.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                        }
                    });
                }
                builder.show();
            }
        });

        binding.setViewModel(mConversationItemsListViewModel);
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ConversationsListActivity.this, MessagesListActivity.class));
            }
        });

        binding.executePendingBindings();
```

### <a name="messages"></a>Messages

#### ConversationView

<a href="layer-atlas/src/main/java/com/layer/ui/conversation/ConversationView.java">ConversationView</a> Comprises of <a href="layer-atlas/src/main/java/com/layer/ui/message/MessageItemsListView.java">MessageItemsListView</a>  and <a href="layer-atlas/src/main/java/com/layer/ui/composebar/ComposeBar.java">ComposeBar</a>. The conversationView has a BindingAdapter

```
     @BindingAdapter(value = {"app:conversation", "app:layerClient", "app:messageItemsListViewModel", "app:query"}, requireAll = false)
    public static void setConversation(ConversationView view, Conversation conversation, LayerClient layerClient, MessageItemsListViewModel viewModel, Query<Message> query) {
    }
```

Which sets the required object needed on MessageItemsListView


```xml
    <layout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>

        <variable
            name="viewModel"
            type="com.layer.ui.conversation.ConversationViewModel"/>
    </data>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <com.layer.ui.conversation.ConversationView
            android:id="@+id/conversation"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:conversation="@{viewModel.conversation}"
            app:layerClient="@{viewModel.layerClient}"
            app:messageItemsListViewModel="@{viewModel.messageItemsListViewModel}"/>
    </LinearLayout>
</layout>

```

##### Java

```java

mConversationView = mActivityMessagesListBinding.conversation;
        mMessageItemsListViewModel = new MessageItemsListViewModel(this, App.getLayerClient(),
                Util.getImageCacheWrapper(), Util.getDateFormatter(this));

        mConversationViewModel = new ConversationViewModel(getApplicationContext(), App.getLayerClient(),
                Util.getCellFactories(App.getLayerClient()), Util.getImageCacheWrapper(),
                Util.getDateFormatter(getApplicationContext()),
                new SwipeableItem.OnItemSwipeListener<Message>() {
                    @Override
                    public void onSwipe(final Message message, int direction) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MessagesListActivity.this)
                                .setMessage(R.string.alert_message_delete_message)
                                .setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO: simply update this one message
                                        mMessageItemsListViewModel.getAdapter().notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                })

                                .setPositiveButton(R.string.alert_button_delete_all_participants, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        message.delete(LayerClient.DeletionMode.ALL_PARTICIPANTS);
                                    }
                                });
                        // User delete is only available if read receipts are enabled
                        if (message.getConversation().isReadReceiptsEnabled()) {
                            builder.setNeutralButton(R.string.alert_button_delete_my_devices, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    message.delete(LayerClient.DeletionMode.ALL_MY_DEVICES);
                                }
                            });
                        }
                        builder.show();
                    }
                });

        mActivityMessagesListBinding.setViewModel(mConversationViewModel);
        setConversation(conversation, conversation != null);
        mActivityMessagesListBinding.executePendingBindings();

```



###MessageItemsListView

The <a href="layer-atlas/src/main/java/com/layer/ui/message/MessageItemsListView.java">MessageItemsListView</a> is list of Messages, rendered by <a href="layer-atlas/src/main/java/com/layer/ui/message/messagetypes/CellFactory.java">CellFactories</a>. MessageItemsListView is used in <a href="layer-atlas/src/main/java/com/layer/ui/conversation/ConversationView.java">ConversationView</a>

##### XML

```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto">

    <data>

        <import type="com.layer.ui.message.MessageItemsListViewModel"/>

        <variable
            name="viewModel"
            type="MessageItemsListViewModel"/>
    </data>

    <merge
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.layer.ui.message.MessageItemsListView
            android:id="@+id/messages_list"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            app:adapter="@{viewModel.adapter}"
            app:cellFactories="@{viewModel.cellFactories}"
            app:itemSwipeListener="@{viewModel.itemSwipeListener}"
            app:layout_constraintBottom_toTopOf="@+id/compose_bar"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent"/>

    </merge>
</layout>
```

MessageItemsListView is use in <a href="layer-atlas/src/main/java/com/layer/ui/conversation/ConversationView.java">ConversationView</a>

#### ComposeBar

The <a href="layer-atlas/src/main/java/com/layer/ui/composebar/ComposeBar.java">ComposeBar</a> is a text entry area for composing messages and a menu of <a href="layer-atlas/src/main/java/com/layer/ui/message/messagetypes/AttachmentSender.java">AttachmentSenders</a>. It is used in <a href="layer-atlas/src/main/java/com/layer/ui/conversation/ConversationView.java">ConversationView</a>

##### XML

```xml
<com.layer.ui.composebar.ComposeBar
            android:id="@+id/compose_bar"
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:hint="@string/messages_compose_bar_hint"
            android:minHeight="48dp"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"/>
```

#### TypingIndicator

The <a href="layer-atlas/src/main/java/com/layer/ui/TypingIndicatorLayout.java">TypingIndicator</a> presents the user with active typists.

##### XML

```xml
<com.layer.ui.TypingIndicatorLayout
    android:id="@+id/typing_indicator"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    />
```

##### Java

```java
mTypingIndicator = new TypingIndicatorLayout(context);
        mTypingIndicator.setTypingIndicatorFactory(new BubbleTypingIndicatorFactory());
        mTypingIndicator.setTypingActivityListener(new TypingIndicatorLayout.TypingActivityListener() {
            @Override
            public void onTypingActivityChange(TypingIndicatorLayout typingIndicator, boolean active, Set<Identity> users) {
                mMessageItemListView.setFooterView(active ? typingIndicator : null, users);
            }
        });
```

### <a name="Message Types"></a>Message Types
By default, Layer UI supports the following types of messages.

| Type            | Description |
| ----------------|-------------|
| Generic         | Default handler for unknown message types. Displays the mimetype and the content size |
| Text            | Handler for `text/plain` content. |
| Location        | Handler for `location/coordinate` content. Given `lat`/`lon` information, displays the location image (from Google maps), with a hyperlink that launches Maps application|
| ThreePartImage  | Handler for 3 part JPEG image, with preview & dimensions. By default, displays the preview image. On tap, downloads and renders the full resolution image |
| SinglePartImage | Handler for any mime type that starts with `image` tag |

We expect to add support for other handlers in future. If you would like to build a handler, please check <a href="docs/Message-Handlers.md">doc on message handlers</a>.

### <a name="Identity"></a>Identity

An application server can directly upload user information to Layer server. This user information is called <a href="https://docs.layer.com/sdk/android/identities">Identity</a>. `AddressBar` and `AvatarView` are controls that are used to render the Identity information.

#### AddressBar
`AddressBar` can be used to show a list of users. For eg, the list of users in a `Conversation` or to show a user list for creating a new `Conversation`.

##### XML

```xml
<com.layer.ui.AddressBar
            android:id="@+id/conversation_launcher"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
```

##### Java

```java
mAddressBar = mActivityMessagesListBinding.conversationLauncher
                .init(App.getLayerClient(), Util.getImageCacheWrapper())
                .setOnConversationClickListener(new AddressBar.OnConversationClickListener() {
                    @Override
                    public void onConversationClick(AddressBar addressBar, Conversation conversation) {
                        setConversation(conversation, true);
                        setTitleFromConversationTitle(true);
                    }
                })
                .setOnParticipantSelectionChangeListener(new AddressBar.OnParticipantSelectionChangeListener() {
                    @Override
                    public void onParticipantSelectionChanged(AddressBar addressBar, final List<Identity> participants) {
                        if (participants.isEmpty()) {
                            setConversation(null, false);
                            return;
                        }
                        try {
                            setConversation(App.getLayerClient().newConversation(new ConversationOptions().distinct(true), new HashSet<>(participants)), false);
                        } catch (LayerConversationException e) {
                            setConversation(e.getConversation(), false);
                        }
                    }
                })
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (mState == UiState.ADDRESS_CONVERSATION_COMPOSER) {
                            mAddressBar.setSuggestionsVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    }
                })
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            setUiState(UiState.CONVERSATION_COMPOSER);
                            setTitleFromConversationTitle(true);
                            return true;
                        }
                        return false;
                    }
                });
```

#### AvatarView
`AvatarView`can be used to show information about one user, or as a cluster of multiple users. `AvatarView` uses <a href="layer-atlas/src/main/java/com/layer/ui/util/imagecache/ImageCacheWrapper.java">ImageCacheWrapper</a> to abstract whatever Image Caching Library. In the implementation in Atlas Messenger, we use [Picasso](https://github.com/square/picasso) to render the avatar image.

##### XML

```xml
         <com.layer.ui.avatar.AvatarView
            android:id="@+id/avatar"
            android:layout_width="@dimen/layer_ui_avatar_width"
            android:layout_height="@dimen/layer_ui_avatar_height" />
```

##### Java

```java
	    // To create an avatar
        //get Avatar from layout and call init
        biding.avatar.init(new AvatarViewModelImpl(imageCachWrapper), new IdentityFormatterImpl());

	    // To set identites meant for the avatar cluster
	    HashSet<Identity> participants = new HashSet<>(conversation.getParticipants());
	    viewHolder.avatar.setParticipants(participants);
```

## <a name="contributing"></a>Contributing
Layer UI is an Open Source project maintained by Layer. Feedback and contributions are always welcome and the maintainers try to process patches as quickly as possible. Feel free to open up a Pull Request or Issue on Github.

## <a name="license"></a>License

Layer UI is licensed under the terms of the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html). Please see the [LICENSE](LICENSE) file for full details.

## <a name="contact"></a>Contact

Layer UI was developed in San Francisco by the Layer team. If you have any technical questions or concerns about this project feel free to reach out to [Layer Support](mailto:support@layer.com).

### <a name="credits"></a>Credits

* [Amar Srinivasan](https://github.com/sriamar)
* [Steven Jones](https://github.com/sjones94549)
* [Peter Elliott](https://github.com/smpete)