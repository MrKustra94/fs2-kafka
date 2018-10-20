package fs2.kafka

import cats.Show
import cats.syntax.show._
import fs2.kafka.internal.instances._
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

sealed abstract class CommittableOffset[F[_]] {
  def topicPartition: TopicPartition

  def offsetAndMetadata: OffsetAndMetadata

  def offsets: Map[TopicPartition, OffsetAndMetadata]

  def batch: CommittableOffsetBatch[F]

  def commit: F[Unit]
}

object CommittableOffset {
  private[kafka] def apply[F[_]](
    topicPartition: TopicPartition,
    offsetAndMetadata: OffsetAndMetadata,
    commit: Map[TopicPartition, OffsetAndMetadata] => F[Unit]
  ): CommittableOffset[F] = {
    val _topicPartition = topicPartition
    val _offsetAndMetadata = offsetAndMetadata
    val _commit = commit

    new CommittableOffset[F] {
      override val topicPartition: TopicPartition =
        _topicPartition

      override val offsetAndMetadata: OffsetAndMetadata =
        _offsetAndMetadata

      override val offsets: Map[TopicPartition, OffsetAndMetadata] =
        Map(_topicPartition -> _offsetAndMetadata)

      override def batch: CommittableOffsetBatch[F] =
        CommittableOffsetBatch(offsets, _commit)

      override def commit: F[Unit] =
        _commit(offsets)

      override def toString: String =
        Show[CommittableOffset[F]].show(this)
    }
  }

  implicit def committableOffsetShow[F[_]]: Show[CommittableOffset[F]] =
    Show.show(co => show"CommittableOffset(${co.topicPartition} -> ${co.offsetAndMetadata})")
}
