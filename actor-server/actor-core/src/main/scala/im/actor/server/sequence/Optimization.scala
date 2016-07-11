package im.actor.server.sequence

import com.google.protobuf.ByteString
import im.actor.api.rpc.counters.UpdateCountersChanged
import im.actor.api.rpc.groups._
import im.actor.api.rpc.sequence.{ ApiUpdateOptimization, UpdateEmptyUpdate }
import im.actor.server.messaging.MessageParsing
import im.actor.server.model.SerializedUpdate

object Optimization extends MessageParsing {
  val GroupV2 = "GROUPS_V2"

  private type UpdateHeader = Int
  private type DeliveryId = String

  type Func = DeliveryId ⇒ (SerializedUpdate ⇒ SerializedUpdate)

  private val emptyUpdate = SerializedUpdate(
    header = UpdateEmptyUpdate.header,
    body = ByteString.copyFrom(UpdateEmptyUpdate.toByteArray)
  )

  private val EmptyFunc: Func = deliveryId ⇒ identity[SerializedUpdate]

  // this is our default client we must support.
  // none of optimizations are applied. new updates are excluded from final sequence, we also can exlude messages by deliveryId
  private val noOptimizationTransformation: Map[ApiUpdateOptimization.ApiUpdateOptimization, Func] = Map(
    ApiUpdateOptimization.STRIP_COUNTERS → EmptyFunc,
    ApiUpdateOptimization.GROUPS_V2 → { deliveryId ⇒ upd ⇒
      val excludeUpdates = Set(
        UpdateGroupAboutChanged.header,
        UpdateGroupAvatarChanged.header,
        UpdateGroupTopicChanged.header,
        UpdateGroupTitleChanged.header,

        UpdateGroupOwnerChanged.header,
        UpdateGroupHistoryShared.header,
        UpdateGroupCanSendMessagesChanged.header,
        UpdateGroupCanViewMembersChanged.header,
        UpdateGroupCanInviteMembersChanged.header,
        UpdateGroupMemberChanged.header,
        UpdateGroupMembersBecameAsync.header,
        UpdateGroupMembersUpdated.header,
        UpdateGroupMemberDiff.header,
        UpdateGroupMembersCountChanged.header,
        UpdateGroupMemberAdminChanged.header
      )
      if (deliveryId.startsWith(GroupV2))
        emptyUpdate
      else
        excludeIfContains(excludeUpdates, upd)
    }
  )

  val Default: Func = { deliveryId ⇒
    Function.chain((noOptimizationTransformation.values map { e ⇒
      e(deliveryId)
    }).toSeq)
  }

  private val optimizationTransformation: Map[ApiUpdateOptimization.ApiUpdateOptimization, Func] = Map(
    ApiUpdateOptimization.STRIP_COUNTERS → { _ ⇒ upd ⇒
      val excludeUpdates = Set(UpdateCountersChanged.header)
      excludeIfContains(excludeUpdates, upd)
    },
    ApiUpdateOptimization.GROUPS_V2 → { _ ⇒ upd ⇒
      val excludeUpdates = Set(
        UpdateGroupInviteObsolete.header,
        UpdateGroupUserInvitedObsolete.header,
        UpdateGroupUserLeaveObsolete.header,
        UpdateGroupUserKickObsolete.header,
        UpdateGroupMembersUpdateObsolete.header,
        UpdateGroupTitleChangedObsolete.header,
        UpdateGroupTopicChangedObsolete.header,
        UpdateGroupAboutChangedObsolete.header,
        UpdateGroupAvatarChangedObsolete.header
      )
      excludeIfContains(excludeUpdates, upd)
    }
  )

  private def excludeIfContains(excludeUpdates: Set[UpdateHeader], upd: SerializedUpdate): SerializedUpdate =
    if (excludeUpdates contains upd.header) emptyUpdate else upd

  def apply(optimizations: Seq[Int]): Func = {
    val enabledOptimizations = optimizations map { optIndex ⇒
      val opt = ApiUpdateOptimization(optIndex)
      opt → optimizationTransformation(opt)
    }
    { deliveryId: String ⇒
      Function.chain(((noOptimizationTransformation ++ enabledOptimizations).values map {
        e ⇒ e(deliveryId)
      }).toSeq)
    }
  }
}
