/* eslint-disable @next/next/no-img-element */

import { NotePreviewType } from '@/interfaces/note/NotePreviewType'
import noteParseDate from '@/utils/noteParseDate'
import LikeSVG from '@/../public/icon/like.svg'
import MsgSVG from '@/../public/icon/message-text-alt.svg'
import { useRouter } from 'next/navigation'
import { Dispatch, SetStateAction, useState } from 'react'
import { useRecoilValue } from 'recoil'
import { codeNameMapState } from '@/recoils/crypto'
import { postNoteLike } from '@/apis/note'

interface NotePreviewProps {
  notePreviewData: NotePreviewType
  setCoinList: Dispatch<SetStateAction<string[]>>
}

function NotePreview({ notePreviewData, setCoinList }: NotePreviewProps) {
  const router = useRouter()
  const coinMap = useRecoilValue(codeNameMapState)
  const [liked, setLiked] = useState(notePreviewData.liked)
  const [likeCnt, setLikeCnt] = useState(notePreviewData.likeCount)

  const buttonDivClasses =
    'flex items-center gap-2 border-solid border-transparent px-2 hover:rounded hover:border-brandColor cursor-pointer'

  const handleClickNotesPreview = () => {
    router.push(`/notes/${notePreviewData.noteId}`)
  }

  const handleClickCoinBtn = (ticker: string) => {
    setCoinList([ticker])
  }

  const handleClickMemberDiv = () => {
    router.push(`/member/${notePreviewData.member.id}`)
  }

  const handleClickLikeDiv = () => {
    postNoteLike(notePreviewData.noteId, () => {
      if (liked) {
        setLiked(false)
        setLikeCnt((prev) => prev - 1)
      } else {
        setLiked(true)
        setLikeCnt((prev) => prev + 1)
      }
    })
  }

  const handleClickMsgDiv = () => {
    router.push(`/notes/${notePreviewData.noteId}?scroll=comments`)
  }

  return (
    <div className="space-y-2 rounded border-2 border-solid border-deactivatedGray bg-brandWhite p-1 hover:border-brandColor">
      <div>
        <img
          src={notePreviewData.thumbnailImage}
          alt="preview"
          className="h-56 w-full cursor-pointer rounded"
          onClick={handleClickNotesPreview}
        />
        <div
          className="mt-2 w-fit cursor-pointer rounded bg-brandColor p-1 text-xs text-brandWhite"
          onClick={() => handleClickCoinBtn(notePreviewData.ticker)}
        >
          {coinMap.get(notePreviewData.ticker)}
        </div>
        <h2
          className="cursor-pointer font-NSB text-2xl"
          onClick={handleClickNotesPreview}
        >
          {notePreviewData.title}
        </h2>
        <div
          className="mb-4 flex cursor-pointer"
          onClick={handleClickMemberDiv}
        >
          <img
            src={notePreviewData.member.profileUrl}
            className="block h-10 w-10 rounded-full"
            alt="profile"
          />
          <div className="ml-2 flex flex-col justify-center text-xs">
            <div>{notePreviewData.member.nickname}</div>
            <div className=" text-brandDarkGray">
              {noteParseDate(notePreviewData.writeDate)}
            </div>
          </div>
        </div>
        <div
          className="relative h-[4.375rem] w-full cursor-pointer overflow-hidden"
          onClick={handleClickNotesPreview}
        >
          <div>{notePreviewData.thumbnailContent}</div>
          <div className="absolute bottom-0 left-0 right-0 h-16 bg-gradient-to-t from-white to-transparent" />
        </div>
      </div>
      <div className="flex justify-end">
        <div className={`${buttonDivClasses}`} onClick={handleClickLikeDiv}>
          <div className="mt-1.5">
            {liked ? (
              <LikeSVG fill="red" />
            ) : (
              <LikeSVG fill="transparent" stroke="black" />
            )}
          </div>
          <div>{likeCnt}</div>
        </div>
        <div className={`${buttonDivClasses}`} onClick={handleClickMsgDiv}>
          <div className="mt-1.5">
            <MsgSVG />
          </div>
          <div>{notePreviewData.commentCount}</div>
        </div>
      </div>
    </div>
  )
}

export default NotePreview
